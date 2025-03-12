package io.littlehorse.document.processing;

import static io.littlehorse.document.processing.LHConstants.ERROR_EXTRACTION_FAILED;
import static io.littlehorse.document.processing.LHConstants.ERROR_VALIDATION_FAILED;
import static io.littlehorse.document.processing.LHConstants.EVENT_DOCUMENT_APPROVAL;
import static io.littlehorse.document.processing.LHConstants.STATUS_EXTRACTION_FAILED;
import static io.littlehorse.document.processing.LHConstants.STATUS_VALIDATION_FAILED;
import static io.littlehorse.document.processing.LHConstants.TASK_DETERMINE_APPROVAL_ROUTE;
import static io.littlehorse.document.processing.LHConstants.TASK_EXTRACT_DOCUMENT_INFO;
import static io.littlehorse.document.processing.LHConstants.TASK_NOTIFY_SUBMITTER;
import static io.littlehorse.document.processing.LHConstants.TASK_ROUTE_TO_DEPARTMENT;
import static io.littlehorse.document.processing.LHConstants.TASK_VALIDATE_DOCUMENT;
import static io.littlehorse.document.processing.LHConstants.WORKFLOW_NAME;

import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class DocumentProcessingWorkflow {
    /**
     * This method defines the document processing workflow logic
     */
    public void documentProcessingWf(WorkflowThread wf) {
        // Declare input variables
        WfRunVariable documentId = wf.declareStr("document-id").searchable().required();
        WfRunVariable documentType = wf.declareStr("document-type").searchable().required();
        WfRunVariable submitterId = wf.declareStr("submitter-id").searchable().required();

        // Declare internal variables
        WfRunVariable documentInfo = wf.declareJsonObj("document-info");
        WfRunVariable isValid = wf.declareBool("is-valid");
        WfRunVariable approvalResult = wf.declareBool("approval-result");
        WfRunVariable approvalRoute = wf.declareStr("approval-route");
        WfRunVariable processingStatus = wf.declareStr("processing-status");

        // Step 1: Extract document information - with retries
        NodeOutput extractedInfo = wf.execute(TASK_EXTRACT_DOCUMENT_INFO, documentId, documentType)
                .withRetries(5); // Retry up to 3 times if the extraction fails

        // Handle extraction failure if it still fails after retries
        wf.handleError(extractedInfo, LHErrorType.TASK_FAILURE, handler -> {
            handler.execute(TASK_NOTIFY_SUBMITTER, submitterId, documentId, STATUS_EXTRACTION_FAILED);
            handler.fail(ERROR_EXTRACTION_FAILED, "Failed to extract document information after retries");
        });

        // Store the extracted document info
        documentInfo.assign(extractedInfo);

        // Step 2: Validate document
        NodeOutput validationResult =
                wf.execute(TASK_VALIDATE_DOCUMENT, documentInfo, documentType).withRetries(5);

        // Handle validation failure
        wf.handleError(validationResult, LHErrorType.TASK_FAILURE, handler -> {
            handler.execute(TASK_NOTIFY_SUBMITTER, submitterId, documentId, STATUS_VALIDATION_FAILED);
            handler.fail(ERROR_VALIDATION_FAILED, "Document validation service failed");
        });

        // Store validation result
        isValid.assign(validationResult);

        // Step 3: Route based on validation
        wf.doIfElse(
                isValid.isEqualTo(true),
                validFlowHandler -> {
                    // Document is valid - determine approval route using AI
                    NodeOutput routeResult = validFlowHandler
                            .execute(TASK_DETERMINE_APPROVAL_ROUTE, documentInfo, documentType)
                            .withRetries(5); // More retries for LLM service which can be
                    // less reliable

                    // Store the approval route
                    approvalRoute.assign(routeResult);

                    // Route document to appropriate department
                    validFlowHandler
                            .execute(TASK_ROUTE_TO_DEPARTMENT, approvalRoute, documentId, documentInfo)
                            .withRetries(5);

                    // Wait for approval event with timeout
                    NodeOutput approvalResultEvent = validFlowHandler.waitForEvent(EVENT_DOCUMENT_APPROVAL);

                    approvalResult.assign(approvalResultEvent);

                    validFlowHandler.doIfElse(
                            approvalResult.isEqualTo(true),
                            approvedFlowHandler -> {
                                // Store processing status
                                // Store processing status
                                processingStatus.assign(LHConstants.STATUS_APPROVED);

                                // Notify submitter of successful processing
                                validFlowHandler
                                        .execute(TASK_NOTIFY_SUBMITTER, submitterId, documentId, processingStatus)
                                        .withRetries(5);
                            },
                            rejectedFlowHandler -> {
                                // Store processing status
                                processingStatus.assign(LHConstants.STATUS_REJECTED);
                                rejectedFlowHandler
                                        .execute(TASK_NOTIFY_SUBMITTER, submitterId, documentId, processingStatus)
                                        .withRetries(5);
                            });
                },
                invalidFlowHandler -> {
                    // Document is invalid - notify submitter
                    processingStatus.assign(LHConstants.STATUS_INVALID);
                    invalidFlowHandler
                            .execute(TASK_NOTIFY_SUBMITTER, submitterId, documentId, processingStatus)
                            .withRetries(5);
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
