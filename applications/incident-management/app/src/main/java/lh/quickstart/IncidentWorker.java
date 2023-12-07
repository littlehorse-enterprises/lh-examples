package lh.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.json.JSONObject; // Ensure the JSON library is added to your build.gradle

public class IncidentWorker {

    @LHTaskMethod("verify-incident")
    public String verifyIncident(String incidentDetails) {
        String incidentType = extractIncidentType(incidentDetails);
        int severityLevel = extractSeverityLevel(incidentDetails);

        boolean isValid = validateIncident(incidentType);
        boolean isKnownIssue = checkKnownIssues(incidentType);

        if (!isValid) {
            return "Invalid Incident Report";
        } else if (isKnownIssue) {
            return "Incident is a Known Issue";
        } else if (severityLevel >= 5) {
            return "Critical Incident Verified: Severity Level " + severityLevel;
        } else {
            return "Non-Critical Incident: Severity Level " + severityLevel;
        }
    }

    private String extractIncidentType(String details) {
        JSONObject incidentJson = new JSONObject(details);
        return incidentJson.optString("type", "Unknown"); // Default to "Unknown" if not found
    }

    private int extractSeverityLevel(String details) {
        JSONObject incidentJson = new JSONObject(details);
        return incidentJson.optInt("severity", 0); // Default to 0 if not found
    }

    private boolean validateIncident(String type) {
        return true; // Validate the incident type (Dummy logic)
    }

    private boolean checkKnownIssues(String type) {
        return false; // Check if the incident type is a known issue (Dummy logic)
    }

    @LHTaskMethod("periodic-check-task")
    public String periodicCheckTask(String incidentId) {
        String currentStatus = checkIncidentStatus(incidentId);

        if ("Resolved".equals(currentStatus)) {
            return "Incident Resolved";
        } else {
            performStatusBasedActions(currentStatus);
            return "Performed periodic check for Incident ID: " + incidentId;
        }
    }

    private String checkIncidentStatus(String incidentId) {
        return "In Progress"; // Check the current status of the incident (Dummy logic)
    }

    private void performStatusBasedActions(String status) {
        // Implement actions based on the incident's status (Dummy logic)
    }
}
