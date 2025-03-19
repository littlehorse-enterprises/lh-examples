package io.littlehorse.customer.support;

/**
 * Constants for LittleHorse workflow and task definitions.
 */
public final class LHConstants {

    // Workflow constants
    public static final String WORKFLOW_NAME = "ai-customer-support-call-actions";

    // Task names
    public static final String TRANSCRIBE_CALL_TASK = "transcribe-call";
    public static final String DETERMINE_ACTIONS_TASK = "determine-actions";
    public static final String RUN_WORKFLOWS_TASK = "run-workflows";
    private LHConstants() {
        // Private constructor to prevent instantiation
    }
}