package io.littlehorse.customer.support;

/**
 * Constants for LittleHorse workflow and `TaskDef`s.
 */
public final class LHConstants {

    // Workflow names
    public static final String WORKFLOW_NAME = "ai-customer-support-call-actions";

    public static final String ADD_NOTE_TO_CUSTOMER_WORKFLOW = "add-note-to-customer";

    public static final String ESCALATE_CASE_WORKFLOW = "escalate-case";

    public static final String SEND_EMAIL_WORKFLOW = "send-email";

    // Task names

    // Used in ai-customer-support-call-actions workflow
    public static final String TRANSCRIBE_CALL_TASK = "transcribe-call";
    public static final String DETERMINE_ACTIONS_TASK = "determine-actions";
    public static final String RUN_WORKFLOWS_TASK = "run-workflows";

    // Used in add-note-to-customer workflow
    public static final String ANALYZE_CUSTOMER_NOTE_TASK = "analyze-customer-note";
    public static final String REDACT_SENSITIVE_INFO_TASK = "redact-sensitive-info";
    public static final String ADD_NOTE_TO_CUSTOMER_TASK = "add-note-to-customer";
    public static final String SCHEDULE_FOLLOW_UP_TASK = "schedule-follow-up";

    // Used in escalate-case workflow
    public static final String ESCALATE_CASE_TASK = "escalate-case-task";

    // Used in multiple workflows
    public static final String AUDIT_LOG_TASK = "audit-log";
    public static final String SEND_EMAIL_TASK = "send-email-task";

    private LHConstants() {
        // Private constructor to prevent instantiation
    }
}
