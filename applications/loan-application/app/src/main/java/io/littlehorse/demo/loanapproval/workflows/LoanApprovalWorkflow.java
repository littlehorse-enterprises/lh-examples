package io.littlehorse.demo.loanapproval.workflows;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class LoanApprovalWorkflow {

    public static final String LOAN_APPROVAL_WF = "loan-approval";

    public static final String CUSTOMER_ID_VAR = "customer-id";
    public static final String LOAN_AMOUNT_VAR = "loan-amount";
    public static final String STATUS_VAR = "status";
    public static final String INTERNAL_RISK_SCORE_VAR = "internal-risk-score";
    public static final String CREDIT_SCORE_VAR = "credit-score";
    public static final String REQUIRED_DOCUMENTS_VAR = "required-documents";
    public static final String PROVIDED_DOCUMENTS_VAR = "provided-documents";

    public static final String FETCH_CREDIT_SCORE_TASK = "fetch-credit-score";
    public static final String CALCULATE_RISK_SCORE_TASK = "calculate-risk-score";
    public static final String DETERMINE_NEEDED_DOCS_TASK = "determine-required-documents";
    public static final String REQUEST_DOCUMENTATION_TASK = "request-documentation";
    public static final String STORE_COMMENTS_TASK = "store-comments-on-application";
    public static final String APPROVE_APPLICATION_TASK = "approve-loan-application";
    public static final String REJECT_APPLICATION_TASK = "reject-loan-application";

    public static final String DOCUMENTS_PROVIDED_EVENT = "documents-provided";

    public static final String MANUAL_APPROVAL_USER_TASK = "manual-loan-approval";

    private WfRunVariable customerId;
    private WfRunVariable loanAmount;

    private WfRunVariable status;
    private WfRunVariable internalRiskScore;
    private WfRunVariable creditScore;
    private WfRunVariable requiredDocuments;

    private LittleHorseBlockingStub client;

    public LoanApprovalWorkflow(LittleHorseBlockingStub client) {
        this.client = client;
    }

    public void registerWorkflow() {
        UserTaskSchema userTaskSchema = new UserTaskSchema(new ManualLoanApprovalResult(), MANUAL_APPROVAL_USER_TASK);
        client.putUserTaskDef(userTaskSchema.compile());

        client.putExternalEventDef(PutExternalEventDefRequest.newBuilder().setName(DOCUMENTS_PROVIDED_EVENT).build());

        client.putWfSpec(Workflow.newWorkflow(LOAN_APPROVAL_WF, this::doWf).compileWorkflow());
    }

    private void doWf(WorkflowThread wf) {
        customerId = wf.addVariable(CUSTOMER_ID_VAR, VariableType.STR).required().searchable();
        loanAmount = wf.addVariable(LOAN_AMOUNT_VAR, VariableType.INT).required();
        status = wf.addVariable(STATUS_VAR, "PENDING_APPROVAL").searchable();

        internalRiskScore = wf.addVariable(INTERNAL_RISK_SCORE_VAR, VariableType.INT);
        creditScore = wf.addVariable(CREDIT_SCORE_VAR, VariableType.INT);
        requiredDocuments = wf.addVariable(REQUIRED_DOCUMENTS_VAR, VariableType.JSON_ARR);

        // Fetch the credit score
        wf.mutate(creditScore, VariableMutationType.ASSIGN, wf.execute(FETCH_CREDIT_SCORE_TASK, customerId));

        // Calculate Risk Score
        NodeOutput riskScoreOutput = wf.execute(CALCULATE_RISK_SCORE_TASK, loanAmount, creditScore);
        wf.mutate(internalRiskScore, VariableMutationType.ASSIGN, riskScoreOutput);

        // If it's a "risky" loan, then we must manually approve it.
        wf.doIf(wf.condition(internalRiskScore, Comparator.GREATER_THAN, 50), this::manuallyApprove);

        // Determine needed documents for this loan
        NodeOutput requiredDocsOutput = wf.execute(DETERMINE_NEEDED_DOCS_TASK, loanAmount, creditScore, customerId);
        wf.mutate(requiredDocuments, VariableMutationType.ASSIGN, requiredDocsOutput);

        // Mark workflow as "pending documentation"
        wf.mutate(status, VariableMutationType.ASSIGN, "PENDING_DOCUMENTATION");

        // Request required documents from customer
        wf.execute(REQUEST_DOCUMENTATION_TASK, customerId, requiredDocuments);

        // Wait for customer to provide docs. Maximum 5 days.
        NodeOutput docsResult = wf.waitForEvent(DOCUMENTS_PROVIDED_EVENT).timeout(60 * 60 * 24 * 5);
        wf.handleError(docsResult, LHErrorType.TIMEOUT, handler -> {
            handler.mutate(status, VariableMutationType.ASSIGN, "CANCELLED");

            handler.execute(REJECT_APPLICATION_TASK, "Customer did not provide required documentation.");
            handler.fail("documentation-timeout", "Customer did not provide required documentation.");
        });

        wf.mutate(status, VariableMutationType.ASSIGN, "APPROVED");
        wf.execute(APPROVE_APPLICATION_TASK, customerId, loanAmount);
    }

    private void manuallyApprove(WorkflowThread wf) {
        // Assign a UserTask to the approval group to approve/reject the transfer.
        LHFormatString notes = wf.format("User {0} requests a loan of ${1}, credit score {2}, risk score {3}.",
                customerId, loanAmount, creditScore, internalRiskScore);

        WfRunVariable manuallyApproved = wf.addVariable("manually-approved", VariableType.BOOL);
        WfRunVariable comments = wf.addVariable("manual-comments", VariableType.STR);

        UserTaskOutput taskResult = wf.assignUserTask(MANUAL_APPROVAL_USER_TASK, null, "approvals-team").withNotes(notes);

        wf.mutate(manuallyApproved, VariableMutationType.ASSIGN, taskResult.jsonPath("$.isApproved"));
        wf.mutate(comments, VariableMutationType.ASSIGN, taskResult.jsonPath("$.comments"));

        wf.execute("store-comments-on-application", comments);

        // Fail the workflow if the user dis-approved the task.
        wf.doIf(wf.condition(manuallyApproved, Comparator.EQUALS, false), ifBody -> {
            wf.mutate(status, VariableMutationType.ASSIGN, "REJECTED");
            wf.execute(REJECT_APPLICATION_TASK, comments);
            wf.fail("application-rejected", "Application was rejected");
        });
    }
}

class ManualLoanApprovalResult {
    public boolean isApproved;
    public String comments;
}
