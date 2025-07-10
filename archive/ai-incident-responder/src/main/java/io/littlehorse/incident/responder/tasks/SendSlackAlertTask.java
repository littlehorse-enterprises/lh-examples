package io.littlehorse.incident.responder.tasks;

import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;

import io.littlehorse.incident.responder.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class SendSlackAlertTask {
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_SEND_SLACK_ALERT)
    public String sendSlackAlert(
            String engineerId, String alertId, String systemName, JsonNode diagnosticInfo, String remediationAttempted)
            throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Slack notification service unavailable");
        }

        String message = formatSlackMessage(engineerId, alertId, systemName, diagnosticInfo, remediationAttempted);

        // Simulate sending Slack message
        System.out.println("[SIMULATED SLACK] -> @" + engineerId + ": " + message);

        return "Successfully sent Slack alert to engineer " + engineerId + " about alert " + alertId;
    }

    private String formatSlackMessage(
            String engineerId,
            String alertId,
            String systemName,
            JsonNode diagnosticInfo,
            String remediationAttempted) {
        StringBuilder message = new StringBuilder();

        if ("REMINDER".equals(remediationAttempted)) {
            message.append("🔔 REMINDER: Incident still requires your attention! 🔔\n");
        } else {
            message.append("🚨 INCIDENT ALERT: Automated remediation failed! 🚨\n");
        }

        message.append("System: ").append(systemName).append("\n");
        message.append("Alert ID: ").append(alertId).append("\n");

        if (diagnosticInfo.has("alertType")) {
            message.append("Alert Type: ")
                    .append(diagnosticInfo.get("alertType").asText())
                    .append("\n");
        }

        if (diagnosticInfo.has("severity")) {
            message.append("Severity: ")
                    .append(diagnosticInfo.get("severity").asText())
                    .append("\n");
        }

        if (diagnosticInfo.has("diagnosticSummary")) {
            message.append("Diagnostic Summary: ")
                    .append(diagnosticInfo.get("diagnosticSummary").asText())
                    .append("\n");
        }

        message.append("Attempted Remediation: ").append(remediationAttempted).append("\n");
        message.append("Status: Manual intervention required\n");
        message.append("Please acknowledge this alert by responding in the thread.");

        return message.toString();
    }
}
