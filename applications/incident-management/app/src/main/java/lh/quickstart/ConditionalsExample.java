package lh.quickstart;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.common.proto.VariableMutationType;

public class ConditionalsExample {

    // Define constants for workflow and task names
    public static final String WF_NAME = "quickstart";
    public static final String VERIFY_TASK = "verify-incident";
    public static final String SEND_CRITICAL_ALERT_TASK = "send-critical-alert";
    public static final String PERIODIC_CHECK_TASK = "periodic-check-task";

    // Method to define the workflow logic
    public void defineWorkflow(WorkflowThread wf) {
        // Create workflow variables for incident details, severity, and resolution status
        WfRunVariable incidentSeverity = wf.addVariable("severity", VariableType.INT);
        WfRunVariable incidentDetails = wf.addVariable("incidentDetails", VariableType.STR);
        WfRunVariable incidentResolved = wf.addVariable("resolved", VariableType.BOOL);
        WfRunVariable verificationResult = wf.addVariable("verificationResult", VariableType.STR);

        // Check if the incident is critical based on severity
        WorkflowCondition isCritical = wf.condition(incidentSeverity, Comparator.GREATER_THAN, 5);

        // Conditional execution based on the severity of the incident
        wf.doIf(
                isCritical,
                ifBody -> {
                    // Execute the task to verify the incident
                    ifBody.execute(VERIFY_TASK, incidentDetails);

                    // Nested conditional to handle critical incidents
                    ifBody.doIf(
                            wf.condition(verificationResult, Comparator.EQUALS, "Critical Incident Verified"),
                            criticalIfBody -> {
                                // Execute the task to send a critical alert
                                criticalIfBody.execute(SEND_CRITICAL_ALERT_TASK, incidentDetails);
                            }
                    );
                }
        );

        // Loop to perform periodic checks until the incident is resolved
        wf.doWhile(
                wf.condition(incidentResolved, Comparator.EQUALS, false),
                loopBody -> {
                    // Execute the task for periodic checks
                    loopBody.execute(PERIODIC_CHECK_TASK, incidentDetails);
                    // Add a delay between checks
                    loopBody.sleepSeconds(30);
                }
        );

        // Register an interrupt handler for when the incident is resolved
        wf.registerInterruptHandler("incident-resolved", handler -> {
            // Update the 'resolved' variable when the incident is resolved
            handler.mutate(incidentResolved, VariableMutationType.ASSIGN, true);
        });
    }

    // Method to create and return a new workflow instance
    public Workflow getWorkflow() {
        // Instantiate and return a new workflow with the defined logic
        return Workflow.newWorkflow(WF_NAME, this::defineWorkflow);
    }
}
