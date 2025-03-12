package io.littlehorse.wealth.management;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.worker.WorkerContext;
import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PortfolioTaskWorkers implements Closeable {

    public static final String FETCH_PORTFOLIO = "fetch-portfolio-info";
    public static final String NOTIFY_CLIENT = "notify-client-reachout-pending";
    public static final String GENERATE_REPORT = "generate-portfolio-report";
    public static final String NOTIFY_ADVISOR = "notify-manager-pending-meeting";
    public static final String REGISTER_MEETING = "register-meeting-to-portfolio";
    public static final String SEND_REPORT = "send-report-to-client";

    private List<LHTaskWorker> workers;

    public PortfolioTaskWorkers(LHConfig config) {
        this.workers = List.of(
                new LHTaskWorker(this, FETCH_PORTFOLIO, config),
                new LHTaskWorker(this, NOTIFY_CLIENT, config),
                new LHTaskWorker(this, GENERATE_REPORT, config),
                new LHTaskWorker(this, NOTIFY_ADVISOR, config),
                new LHTaskWorker(this, SEND_REPORT, config),
                new LHTaskWorker(this, REGISTER_MEETING, config));
    }

    @LHTaskMethod(FETCH_PORTFOLIO)
    public PortfolioInfo fetchPortfolio(String portfolioId) {
        // In real life, this would fetch a portfolio from a database like Postgres or Yugabyte
        PortfolioInfo result = new PortfolioInfo();
        if (portfolioId.equals("123")) {
            result.managerId = "dave-ramsey";
            result.previousDayClose = Double.valueOf(11_000_000.0);
            result.currentValue = Double.valueOf(10_000_000.0);
        } else {
            result.managerId = "benjamin-graham";
            result.previousDayClose = Double.valueOf(1_000_000.0);
            result.currentValue = Double.valueOf(700_000.0);
        }
        return result;
    }

    @LHTaskMethod(NOTIFY_CLIENT)
    public void notifyClient(String portfolioId) {
        // In real life this would probably send a push notification through a mobile app
        System.out.println(
                "Sending a notification email to client " + portfolioId + " that we will be reaching out soon.");
    }

    @LHTaskMethod(GENERATE_REPORT)
    public String generateReport(String portfolioId) {
        // Simulates generating a report and saving it in a database
        String result = UUID.randomUUID().toString();
        System.out.println("Generated report " + result + " for portfolio " + portfolioId);
        return result;
    }

    @LHTaskMethod(NOTIFY_ADVISOR)
    public void notifyManager(String portfolioId, WorkerContext context) {
        // This Task is used by our WfSpec to send a notification to the responsible manager
        // when they need to reach out to a client.
        String message = "Please reach out to portfolio " + portfolioId + " about recent activity.";

        // Due to the way we have structured our WfSpec, the UserTaskRun could be assigned to:
        // - user_group and user_id
        // - just user_group
        if (context.getUserId() != null) {
            System.out.println("Sending message to " + context.getUserId() + ": " + message);
        } else {
            System.out.println("Sending message to " + context.getUserGroup() + " group: " + message);
        }
    }

    @LHTaskMethod(SEND_REPORT)
    public void sendReportToClient(String reportId, String portfolioId) {
        System.out.println("Sending report " + reportId + " to client " + portfolioId);
    }

    @LHTaskMethod(REGISTER_MEETING)
    public String registerMeeting(MeetingConfirmationInfo meeting) {
        // Save the meeting results into a CRM.
        String result = "Saving meeting into CRM: " + meeting.zoomLink + ", " + meeting.date;
        System.out.println(result);
        return result;
    }

    public void registerAllWorkersAndExternalEvents() {
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }
    }

    public void startWorkers() {
        for (LHTaskWorker worker : workers) {
            worker.start();
        }
    }

    @Override
    public void close() {}
}

// LittleHorse SDK automatically serializes and deserializes POJO's to JSON.
// You can even transfer data across task workers in different languages.
class PortfolioInfo {
    public String managerId;
    public Double currentValue;
    public Double previousDayClose;
}

// External Event Payload
class MeetingConfirmationInfo {
    public Date date;
    public String zoomLink;
}
