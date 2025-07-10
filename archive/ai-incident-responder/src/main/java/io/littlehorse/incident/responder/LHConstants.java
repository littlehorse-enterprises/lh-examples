package io.littlehorse.incident.responder;

/**
 * Constants for LittleHorse workflow and task definitions.
 */
public final class LHConstants {

    // Workflow constants
    public static final String WORKFLOW_NAME = "ai-incident-response";

    // Task names
    public static final String TASK_DIAGNOSE_INCIDENT = "diagnose-incident";
    public static final String TASK_VALIDATE_INCIDENT = "validate-incident";
    public static final String TASK_ATTEMPT_REMEDIATION = "attempt-remediation";
    public static final String TASK_ESCALATE_TO_ENGINEER = "escalate-to-engineer";
    public static final String TASK_NOTIFY_STATUS = "notify-status";
    public static final String TASK_SEND_SLACK_ALERT = "send-slack-alert";

    // External event definitions
    public static final String EVENT_ENGINEER_RESPONSE = "engineer-response";

    // Status values
    public static final String STATUS_FIXED = "FIXED";
    public static final String STATUS_ESCALATED = "ESCALATED";
    public static final String STATUS_FALSE_ALARM = "FALSE_ALARM";
    public static final String STATUS_MANUAL_INTERVENTION = "MANUAL_INTERVENTION";
    
    // Error codes
    public static final String ERROR_DIAGNOSIS_FAILED = "diagnosis-failed";
    public static final String ERROR_REMEDIATION_FAILED = "remediation-failed";
    public static final String ERROR_VALIDATION_FAILED = "validation-failed";

    private LHConstants() {
        // Private constructor to prevent instantiation
    }
}