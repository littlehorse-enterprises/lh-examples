package lh.it.demo;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.json.JSONObject;

public class IncidentWorker {

    /**
     * Task method to verify an incident based on its details.
     *
     * @param incidentDetails JSON string containing incident data.
     * @return A verification result as a String.
     */
    @LHTaskMethod("verify-incident")
    public String verifyIncident(String incidentDetails) {
        // Extract incident type and severity level from the JSON details
        String incidentType = extractIncidentType(incidentDetails);
        int severityLevel = extractSeverityLevel(incidentDetails);

        // Return a string combining the incident type and severity level
        return "Incident Verified: Type - " + incidentType + ", Severity Level - " + severityLevel;
    }

    /**
     * Extracts the incident type from JSON details.
     *
     * @param details JSON string containing incident data.
     * @return The type of the incident.
     */
    private String extractIncidentType(String details) {
        JSONObject incidentJson = new JSONObject(details);
        return incidentJson.optString("type", "Unknown"); // Default to "Unknown" if not found
    }

    /**
     * Extracts the severity level from JSON details.
     *
     * @param details JSON string containing incident data.
     * @return The severity level of the incident.
     */
    private int extractSeverityLevel(String details) {
        JSONObject incidentJson = new JSONObject(details);
        return incidentJson.optInt("severity", 0); // Default to 0 if not found
    }

    /**
     * Task method for performing periodic checks on an incident.
     *
     * @param incidentId ID of the incident to check.
     * @return A string indicating completion of the periodic check.
     */
    @LHTaskMethod("periodic-check-task")
    public String periodicCheckTask(String incidentId) {
        // Simulate the process of a periodic check
        System.out.println("Performing periodic check for Incident ID: " + incidentId);
        return "Periodic Check Completed";
    }

    /**
     * Task method to send critical alerts for incidents.
     *
     * @param incidentDetails JSON string containing incident data.
     * @return A string indicating that the alert was sent.
     */
    @LHTaskMethod("send-critical-alert")
    public String sendCriticalAlert(String incidentDetails) {
        // Simulate sending a critical alert
        System.out.println("CRITICAL INCIDENT ALERT: " + incidentDetails);
        return "Alert sent";
    }

    /**
     * Simulates checking the current status of an incident.
     *
     * @param incidentId ID of the incident to check.
     * @return The current status of the incident.
     */
    private String checkIncidentStatus(String incidentId) {
        // Dummy implementation for checking incident status
        return "In Progress";
    }
}
