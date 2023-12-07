package lh.quickstart;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.common.proto.VariableMutationType;

public class QuickstartWorkflow {

    public static final String WF_Name = "quickstart";
    public static final String VERIFY_TASK = "verify-incident";
    public static final String PERIODIC_CHECK_TASK = "periodic-check-task";

    public void defineWorkflow(WorkflowThread wf) {
        WfRunVariable incidentSeverity = wf.addVariable("severity", VariableType.INT);
        WfRunVariable incidentDetails = wf.addVariable("incidentDetails", VariableType.STR);
        WfRunVariable incidentResolved = wf.addVariable("resolved", VariableType.BOOL);

        WorkflowCondition isCritical = wf.condition(incidentSeverity, Comparator.GREATER_THAN, 5);

        wf.doIf(
                isCritical,
                ifBody -> {
                    ifBody.execute(VERIFY_TASK, incidentDetails);
                }
        );

        wf.doWhile(
                wf.condition(incidentResolved, Comparator.EQUALS, false),
                loopBody -> {
                    loopBody.execute(PERIODIC_CHECK_TASK, incidentDetails);
                    // Implement wait logic if required
                }
        );

        // Register an interrupt handler for 'incident-resolved'
        wf.registerInterruptHandler("incident-resolved", handler -> {
            handler.mutate(incidentResolved, VariableMutationType.ASSIGN, true);
        });
    }

    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WF_Name, this::defineWorkflow);
    }
}
