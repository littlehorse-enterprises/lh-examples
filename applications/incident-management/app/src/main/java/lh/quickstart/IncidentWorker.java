package lh.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.json.JSONObject; // Utilize the JSON library for parsing JSON strings

public class IncidentWorker {

    // Task method to verify an incident
    @LHTaskMethod("verify-incident")
    public String verifyIncident(String incidentDetails) {
        // Extract incident type and severity from incidentDetails
        String incidentType = extractIncidentType(incidentDetails);
        int severityLevel = extractSeverityLevel(incidentDetails);

        // Validate the incident and check if it's a known issue
        boolean isValid = validateIncident(incidentType);
        boolean isKnownIssue = checkKnownIssues(incidentType);

        // Determine verification result based on the checks above
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

    // Method to extract the incident type from the JSON details
    private String extractIncidentType(String details) {
        JSONObject incidentJson = new JSONObject(details);
        return incidentJson.optString("type", "Unknown"); // Default to "Unknown" if not found
    }

    // Method to extract the severity level from the JSON details
    private int extractSeverityLevel(String details) {
        JSONObject incidentJson = new JSONObject(details);
        return incidentJson.optInt("severity", 0); // Default to 0 if not found
    }

    // Dummy method to validate the incident type
    private boolean validateIncident(String type) {
        return true; // Assuming the incident type is valid
    }

    // Dummy method to check if the incident type is a known issue
    private boolean checkKnownIssues(String type) {
        return false; // Assuming this is not a known issue
    }

    // Task method for periodic checks on an incident
    @LHTaskMethod("periodic-check-task")
    public String periodicCheckTask(String incidentDetails) throws InterruptedException {
        JSONObject incidentJson = new JSONObject(incidentDetails);
        boolean isCritical = incidentJson.optInt("severity", 0) >= 5;
        boolean resolved = false;

        // Loop to perform actions until the incident is resolved
        while (!resolved) {
            String currentStatus = checkIncidentStatus(incidentJson.getString("incidentId"));
            resolved = "Resolved".equals(currentStatus);

            if (!resolved) {
                // Send notifications based on severity
                if (isCritical) {
                    sendCriticalAlert(incidentJson.getString("incidentId"));
                } else {
                    sendNotCriticalAlert(incidentJson.getString("incidentId"));
                }
                //Thread.sleep(30000); // Sleep for 30 seconds between notifications
            }
        }

        return "Incident Resolved";
    }

    // Method to send critical alerts (dummy implementation)
    private void sendCriticalAlert(String incidentId) {
        System.out.println("CRITICAL ALERT: Incident " + incidentId + " requires immediate attention!");
    }

    // Method to send non-critical alerts (dummy implementation)
    private void sendNotCriticalAlert(String incidentId) {
        System.out.println("Alert: Incident " + incidentId + " is not critical but needs to be monitored.");
    }

    // Dummy method to simulate checking the incident status
    private String checkIncidentStatus(String incidentId) {
        return "In Progress"; // Example status
    }

    // Method placeholder for performing actions based on the incident's status
    private void performStatusBasedActions(String status) {
        // Here, we would implement actions based on the incident's status
    }
}
