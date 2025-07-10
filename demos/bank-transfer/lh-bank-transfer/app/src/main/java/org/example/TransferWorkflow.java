package org.example;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import java.io.IOException;

public class TransferWorkflow {

    private final LHConfig config;
    public static final String USER_TASKS_FORM = "user-tasks-form";
    private static final UserTaskSchema userTaskSchema = new UserTaskSchema(new UserTasksForm(), USER_TASKS_FORM);
    Double TRANSFER_CAP = 50000.00;

    public TransferWorkflow(LHConfig config) {
        this.config = config;
    }

    public void registerWorkflow() throws IOException {
        Workflow wf = Workflow.newWorkflow("initiate-transfer", this::wfLogic);

        wf.registerWfSpec(config.getBlockingStub());
    }

    // This method uses the LH DSL
    // (https://littlehorse.dev/docs/developer-guide/wfspec-development)
    // to define a WfSpec.
    private void wfLogic(WorkflowThread wf) {
        // Input should be:
        //        {
        //            "fromAccountId": "1000000001",
        //                "toAccountId": "1000000002",
        //                "amount": 123.42,
        //                "currency": "THB",
        //                "description": "Test 2"
        //        }

        WfRunVariable transferDetails = wf.addVariable("transferDetails", VariableType.JSON_OBJ)
                .searchable()
                .required();

        WfRunVariable fromAccount = wf.addVariable("from-account", VariableType.JSON_OBJ);
        WfRunVariable toAccount = wf.addVariable("to-account", VariableType.JSON_OBJ);

        // Fetch fromAccount and check if it's valid
        NodeOutput fromAccountOutput = wf.execute("fetch-account", transferDetails.jsonPath("$.fromAccountId"))
                .withRetries(100)
                .withExponentialBackoff(ExponentialBackoffRetryPolicy.newBuilder()
                        .setBaseIntervalMs(100)
                        .setMultiplier(2.0f)
                        .setMaxDelayMs(1000)
                        .build());
        wf.mutate(fromAccount, VariableMutationType.ASSIGN, fromAccountOutput);
        wf.doIf(wf.condition(fromAccount.jsonPath("$.accountStatus"), Comparator.NOT_EQUALS, "ACTIVE"), handler -> {
            handler.throwEvent(
                    "transfer-failed",
                    wf.format(
                            "Account {0} was in the status {1}",
                            transferDetails.jsonPath("$.fromAccountId"), fromAccount.jsonPath("$.accountStatus")));
            handler.fail("invalid-from-account", "Inactive from account");
        });

        // Fetch toAccountId and check if it's valid
        NodeOutput toAccountOutput = wf.execute("fetch-account", transferDetails.jsonPath("$.toAccountId"))
                .withRetries(100)
                .withExponentialBackoff(ExponentialBackoffRetryPolicy.newBuilder()
                        .setBaseIntervalMs(100)
                        .setMultiplier(2.0f)
                        .setMaxDelayMs(1000)
                        .build());
        wf.mutate(toAccount, VariableMutationType.ASSIGN, toAccountOutput);
        wf.doIf(wf.condition(toAccount.jsonPath("$.accountStatus"), Comparator.NOT_EQUALS, "ACTIVE"), handler -> {
            handler.throwEvent(
                    "transfer-failed",
                    wf.format(
                            "Account {0} was in the status {1}",
                            transferDetails.jsonPath("$.toAccountId"), toAccount.jsonPath("$.accountStatus")));
            handler.fail("invalid-to-account", "Inactive to account");
        });

        WfRunVariable isTransferApproved = wf.addVariable("isTransferApproved", VariableType.BOOL);

        wf.doIf(
                wf.condition(transferDetails.jsonPath(("$.amount")), Comparator.GREATER_THAN_EQ, TRANSFER_CAP),
                handler -> {
                    UserTaskOutput formResults = handler.assignUserTask(USER_TASKS_FORM, null, "admins")
                            .withNotes(wf.format(
                                    "From Account {0} \n is requesting to send {1}{2} \n to {3}",
                                    transferDetails.jsonPath("$.fromAccountId"),
                                    transferDetails.jsonPath("$.amount"),
                                    transferDetails.jsonPath("$.currency"),
                                    transferDetails.jsonPath("$.toAccountId")));

                    wf.mutate(
                            isTransferApproved,
                            VariableMutationType.ASSIGN,
                            formResults.jsonPath(
                                    "$.isApproved")); // this throws an exception if $.isApproved doesn't exist
                    handler.doIf(handler.condition(isTransferApproved, Comparator.EQUALS, false), handler2 -> {
                        handler2.fail("transfer-denied", "Transfer denied by Admins");
                    });
                });

        // execute the transfer
        NodeOutput transferOutput = wf.execute(
                "initiate-transfer",
                transferDetails.jsonPath("$.fromAccountId"),
                transferDetails.jsonPath("$.toAccountId"),
                transferDetails.jsonPath("$.amount"),
                transferDetails.jsonPath("$.currency"),
                transferDetails.jsonPath("$.description"));

        // Get the transferId back so that we check for completion of failure.
        WfRunVariable transferId =
                wf.addVariable("transfer-id", VariableType.STR).searchable();
        wf.mutate(transferId, VariableMutationType.ASSIGN, transferOutput.jsonPath("$.transferId"));

        // check transfer status
        WfRunVariable transferStatus = wf.addVariable("status", "PENDING").searchable();

        wf.doWhile(wf.condition(transferStatus, Comparator.EQUALS, "PENDING"), loop -> {
            NodeOutput transferStatusOutput = wf.execute("check-transfer", transferId);
            wf.mutate(transferStatus, VariableMutationType.ASSIGN, transferStatusOutput.jsonPath("$.status"));
            wf.sleepSeconds(10);
        });

        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();
        client.putWorkflowEventDef(PutWorkflowEventDefRequest.newBuilder()
                .setName("transfer-completed")
                .build());
        client.putWorkflowEventDef(PutWorkflowEventDefRequest.newBuilder()
                .setName("transfer-failed")
                .build());

        wf.doIfElse(
                wf.condition(transferStatus, Comparator.EQUALS, "COMPLETED"),
                successHandler -> {
                    successHandler.throwEvent("transfer-completed", "all good");
                },
                failureHandler -> {
                    failureHandler.throwEvent("transfer-failed", "Transfer status: ERROR");
                });
    }

    public static class UserTasksForm {
        @UserTaskField(
                displayName = "Approved?",
                description = "Reply 'true' if this is an acceptable request.",
                required = true // if this
                )
        public boolean isApproved;
    }
}
