package io.littlehorse.document.processing;

import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class DocumentProcessingWorkflow {
    static final String WORKFLOW_NAME = "document-processing";
    static final String DOCUMENT_APPROVED_EVENT = "document-approved";

    /**
     * This method defines the document processing workflow logic
     */
    public void documentProcessingWf(WorkflowThread wf) {
        // Declare input variables
        WfRunVariable documentId = wf.declareStr("document-id").searchable().required();
        WfRunVariable documentType = wf.declareStr("document-type").searchable().required();
        WfRunVariable submitterId = wf.declareStr("submitter-id").searchable().required();

        // Declare internal variables
        WfRunVariable extractedInfo = wf.declareJsonObj("extracted-info");
        WfRunVariable isValid = wf.declareBool("is-valid");
        WfRunVariable department = wf.declareStr("department");
        WfRunVariable routingResult = wf.declareStr("routing-result");

        // Step 1: Extract document information
        NodeOutput extractResult = wf.execute("extract-document-info", documentId, documentType)
                .withRetries(3);

        extractedInfo.assign(extractResult);

        // Log the extracted information
        wf.execute("log-message", "Extracted info: " + extractedInfo);

        // Step 2: Validate the document
        NodeOutput validationResult = wf.execute("validate-document", extractedInfo, documentType)
                .withRetries(3);

        isValid.assign(validationResult);

        // Step 3: Process based on validation result
        wf.doIfElse(
                isValid.isEqualTo(true),
                ifBody -> {
                    // Step 4: Determine approval route using LLM
                    NodeOutput approvalRoute = ifBody.execute("determine-approval-route", extractedInfo, documentType)
                            .withRetries(5);

                    department.assign(approvalRoute);

                    // Log the determined department
                    ifBody.execute("log-message", "Document needs approval from: " + department);

                    // Step 5: Route to appropriate department
                    ifBody.doIf(department.isEqualTo("FINANCE"), thenBody -> {
                        NodeOutput routeResult = thenBody.execute("route-to-finance", documentId, extractedInfo)
                                .withRetries(3);
                        routingResult.assign(routeResult);
                    });

                    ifBody.doIf(department.isEqualTo("LEGAL"), thenBody -> {
                        NodeOutput routeResult = thenBody.execute("route-to-legal", documentId, extractedInfo)
                                .withRetries(3);
                        routingResult.assign(routeResult);
                    });

                    ifBody.doIf(department.isEqualTo("HR"), thenBody -> {
                        NodeOutput routeResult = thenBody.execute("route-to-hr", documentId, extractedInfo)
                                .withRetries(3);
                        routingResult.assign(routeResult);
                    });

                    // Step 6: Wait for approval decision
                    NodeOutput approvalResult = ifBody.waitForEvent(DOCUMENT_APPROVED_EVENT)
                            .timeout(60 * 60 * 24 * 3); // 3 days timeout

                    // Handle timeout
                    ifBody.handleError(approvalResult, LHErrorType.TIMEOUT, handler -> {
                        handler.execute("notify-submitter", submitterId, documentId, "APPROVAL_TIMEOUT");
                        handler.fail("approval-timeout", "Document approval timed out after 3 days");
                    });

                    // Step 7: Notify submitter of successful processing
                    ifBody.execute("notify-submitter", submitterId, documentId, "APPROVED");
                },
                elseBody -> {
                    // If document is invalid, notify submitter
                    elseBody.execute("notify-submitter", submitterId, documentId, "INVALID");
                });
    }

    /**
     * Returns a LittleHorse Workflow wrapper object that can be used to register
     * the WfSpec.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WORKFLOW_NAME, this::documentProcessingWf);
    }
}
