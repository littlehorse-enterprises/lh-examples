package lh.quickstart;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.common.proto.VariableMutationType;

public class QuickstartWorkflow {

    // Constants for workflow and task names
    public static final String WF_Name = "quickstart";
    public static final String VERIFY_TASK = "verify-incident";
    public static final String PERIODIC_CHECK_TASK = "periodic-check-task";

    // Method to define the workflow logic
    public void defineWorkflow(WorkflowThread wf) {
        // Define workflow variables
        WfRunVariable incidentSeverity = wf.addVariable("severity", VariableType.INT);
        WfRunVariable incidentDetails = wf.addVariable("incidentDetails", VariableType.STR);
        WfRunVariable incidentResolved = wf.addVariable("resolved", VariableType.BOOL);

        // Define condition to check if incident severity is critical (greater than 5)
        WorkflowCondition isCritical = wf.condition(incidentSeverity, Comparator.GREATER_THAN, 5);

        // Conditional execution: Execute VERIFY_TASK if incident is critical
        wf.doIf(
                isCritical,
                ifBody -> {
                    ifBody.execute(VERIFY_TASK, incidentDetails);
                }
        );

        // Loop execution: Continuously execute PERIODIC_CHECK_TASK until incident is resolved
        wf.doWhile(
                wf.condition(incidentResolved, Comparator.EQUALS, false),
                loopBody -> {
                    loopBody.execute(PERIODIC_CHECK_TASK, incidentDetails);
                    loopBody.sleepSeconds(30);
                }
        );

        // Register an interrupt handler for resolving the incident
        wf.registerInterruptHandler("incident-resolved", handler -> {
            // Mutate the 'resolved' variable to true when the incident is resolved
            handler.mutate(incidentResolved, VariableMutationType.ASSIGN, true);
        });
    }

    // Method to create and return a new workflow instance
    public Workflow getWorkflow() {
        // Instantiate and return a new workflow with the defined logic
        return Workflow.newWorkflow(WF_Name, this::defineWorkflow);
    }
}
