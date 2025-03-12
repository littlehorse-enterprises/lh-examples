package io.littlehorse.document.processing;

/**
 * Constants for LittleHorse workflow and task definitions.
 */
public final class LHConstants {

    // Workflow constants
    public static final String WORKFLOW_NAME = "document-processing";

    // Task names
    public static final String TASK_EXTRACT_DOCUMENT_INFO = "extract-document-info";
    public static final String TASK_VALIDATE_DOCUMENT = "validate-document";
    public static final String TASK_DETERMINE_APPROVAL_ROUTE = "determine-approval-route";
    public static final String TASK_ROUTE_TO_DEPARTMENT = "route-to-department";
    public static final String TASK_NOTIFY_SUBMITTER = "notify-submitter";
    public static final String TASK_NOTIFY_APPROVER_REMINDER = "notify-approver-reminder";

    // External event definitions
    public static final String EVENT_DOCUMENT_APPROVAL = "document-approval";

    // Status values
    public static final String STATUS_EXTRACTION_FAILED = "EXTRACTION_FAILED";
    public static final String STATUS_VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_INVALID = "INVALID";

    // Error codes
    public static final String ERROR_EXTRACTION_FAILED = "extraction-failed";
    public static final String ERROR_VALIDATION_FAILED = "validation-failed";

    private LHConstants() {
        // Private constructor to prevent instantiation
    }
}
