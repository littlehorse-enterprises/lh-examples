package io.littlehorse.wealth.management;

import static io.littlehorse.sdk.common.proto.Comparator.GREATER_THAN;
import static io.littlehorse.wealth.management.PortfolioTaskWorkers.*;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class PortfolioWorkflow {

    public static final String WF_NAME = "portfolio-notification";
    public static final String USER_TASK_NAME = "reach-out-to-client";
    public static final String MEETING_SCHEDULED = "meeting-scheduled";

    private final LittleHorseBlockingStub client;

    private WfRunVariable portfolioId;
    private WfRunVariable reportId;
    private WfRunVariable managerId;
    private WfRunVariable currentValue;
    private WfRunVariable valueYesterday;

    public PortfolioWorkflow(final LHConfig config) {
        this.client = config.getBlockingStub();
    }

    // This function defines the logic of our workflow. It gets compiled by
    // the LittleHorse SDK into a `WfSpec`.
    private void wfLogic(WorkflowThread wf) {
        portfolioId = wf.declareStr("portfolio-id").required().asPublic().searchable();
        managerId = wf.declareStr("manager-id").searchable();
        reportId = wf.declareStr("report-id");

        currentValue = wf.declareDouble("current-value");
        valueYesterday = wf.declareDouble("previous-close");

        NodeOutput portfolioValues = wf.execute(FETCH_PORTFOLIO, portfolioId);

        managerId.assign(portfolioValues.jsonPath("$.managerId"));
        currentValue.assign(portfolioValues.jsonPath("$.currentValue"));
        valueYesterday.assign(portfolioValues.jsonPath("$.previousDayClose"));

        var percentageDrop = wf.declareDouble("percentage-drop");
        percentageDrop.assign(wf.subtract(1.0, currentValue.divide(valueYesterday)));

        wf.doIf(percentageDrop.isGreaterThan(0.05), subWf -> {
            // If it is a high-value client, we reach out directly. Otherwise
            // we just send the report
            reportId.assign(subWf.execute(GENERATE_REPORT, portfolioId));

            subWf.doIfElse(
                    subWf.condition(valueYesterday, GREATER_THAN, 10_000_000.0),
                    this::reachOutToClient,
                    this::sendReport);
        });
    }

    public void reachOutToClient(WorkflowThread wf) {
        // Tell client that we will reach out
        wf.execute(NOTIFY_CLIENT, portfolioId);

        // Ask the primary wealth manager to reach out to the client.
        String userGroup = "wealth-advisors";
        var userTaskHandle = wf.assignUserTask(USER_TASK_NAME, managerId, userGroup)
                .withNotes(wf.format(
                        "Portfolio {0} has dropped significantly, please reach out to the owner. Report ID: {1}",
                        portfolioId, reportId));

        // Reassign to the "wealth advisors" group in one day if not completed. For demo purposes,
        // we use only 60 seconds not a full day.
        int oneDaySeconds = 60;
        wf.releaseToGroupOnDeadline(userTaskHandle, oneDaySeconds);

        // Always notify the assigned advisor(s) that they have to reach out to the client
        wf.scheduleReminderTask(userTaskHandle, 0, NOTIFY_ADVISOR, portfolioId);
        wf.scheduleReminderTaskOnAssignment(userTaskHandle, 0, NOTIFY_ADVISOR, portfolioId);

        // Wait for the meeting to occur, and save the results
        NodeOutput meetingResult = wf.waitForEvent(MEETING_SCHEDULED);
        wf.execute(REGISTER_MEETING, meetingResult);
    }

    public void sendReport(WorkflowThread wf) {
        wf.execute(SEND_REPORT, reportId, portfolioId);
    }

    public void registerWorkflow() {
        // Register our UserTask
        UserTaskSchema userTaskDef = new UserTaskSchema(new OutreachResult(), USER_TASK_NAME);
        client.putUserTaskDef(userTaskDef.compile());

        // Register our ExternalEventDef
        client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                .setName(MEETING_SCHEDULED)
                .build());

        Workflow wf = Workflow.newWorkflow(WF_NAME, this::wfLogic);
        wf.registerWfSpec(client);
    }
}

// User Task Form
class OutreachResult {

    @UserTaskField(displayName = "Notes from contact", required = false)
    public String notes;
}
