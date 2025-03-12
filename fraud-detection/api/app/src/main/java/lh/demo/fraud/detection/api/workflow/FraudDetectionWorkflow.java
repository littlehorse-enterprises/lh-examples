package lh.demo.fraud.detection.api.workflow;

import static lh.demo.fraud.detection.api.task.TransactionTasks.*;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.*;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FraudDetectionWorkflow {

    private final LittleHorseBlockingStub client;

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionWorkflow.class);
    public static final String WORKFLOW_NAME = "fraud-detection";

    public FraudDetectionWorkflow(LittleHorseBlockingStub client) {
        this.client = client;
    }

    @PostConstruct
    public void registerWorkflow() {
        UserTaskSchema approvalForm = new UserTaskSchema(new FraudTicketForm(), FraudTicketForm.FRAUD_TICKET_FORM_NAME);

        log.info("Deploying User Task Schemas");
        client.putUserTaskDef(approvalForm.compile());

        log.info("Deploying workflow!");
        Workflow wf = new WorkflowImpl(WORKFLOW_NAME, this::wfFunc);
        wf.registerWfSpec(client);
    }

    private WfRunVariable transactionId;
    private WfRunVariable fraudTicketApproved;
    private WfRunVariable sourceAccount;
    private WfRunVariable destinationAccount;
    private WfRunVariable amount;
    private WfRunVariable possibleFraudDetected;

    public void wfFunc(WorkflowThread wf) {
        sourceAccount = wf.addVariable("source-account", VariableType.STR).required();
        destinationAccount =
                wf.addVariable("destination-account", VariableType.STR).required();
        amount = wf.addVariable("amount", VariableType.INT).required();
        transactionId = wf.addVariable("transaction-id", VariableType.STR).required();
        possibleFraudDetected =
                wf.addVariable("possible-fraud-detected", VariableType.BOOL).searchable();
        fraudTicketApproved = wf.addVariable("fraud-ticket-approved", VariableType.BOOL);

        wf.execute(SAVE_TRANSACTION, transactionId, sourceAccount, destinationAccount, amount);

        NodeOutput detectFraudOutput = wf.execute(DETECT_FRAUD, transactionId);
        wf.mutate(possibleFraudDetected, VariableMutationType.ASSIGN, detectFraudOutput);

        wf.doIf(wf.condition(possibleFraudDetected, Comparator.EQUALS, true), this::reviewFraudulentTransaction);

        wf.execute(APPROVE_TRANSACTION, transactionId);
    }

    private void reviewFraudulentTransaction(WorkflowThread wf) {
        String assignedUserId = null;
        String userGroup = "support";

        UserTaskOutput fraudTicketOutput =
                wf.assignUserTask(FraudTicketForm.FRAUD_TICKET_FORM_NAME, assignedUserId, userGroup);
        wf.mutate(fraudTicketApproved, VariableMutationType.ASSIGN, fraudTicketOutput.jsonPath("$.isApproved"));

        wf.doIf(wf.condition(fraudTicketApproved, Comparator.EQUALS, false), ifBody -> {
            // The transaction was rejected.
            ifBody.execute(REJECT_TRANSACTION, transactionId);
            // Stop the workflow here.
            ifBody.complete();
        });
    }
}
