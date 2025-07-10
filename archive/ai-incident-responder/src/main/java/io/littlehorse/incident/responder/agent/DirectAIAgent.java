package io.littlehorse.incident.responder.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Random;

/**
 * This class demonstrates an AI agent that directly handles incident response
 * without proper orchestration, showing vulnerability to failures.
 */
public class DirectAIAgent {
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public DirectAIAgent() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Process an incident alert without proper orchestration or failure handling
     */
    public void respondToIncident(String alertId, String systemName, String alertType, String severity)
            throws Exception {

        System.out.println("Starting incident response for alert: " + alertId);

        // Step 1: Diagnose incident using AI (API call that might fail)
        JsonNode diagnosticInfo = diagnoseIncident(alertId, systemName, alertType, severity);

        // Step 2: Validate if it's a real incident (API call that might fail)
        boolean isRealIncident = validateIncident(diagnosticInfo, alertType);

        if (!isRealIncident) {
            System.out.println("False alarm detected. Logging and closing incident.");
            notifyStatus(alertId, systemName, "FALSE_ALARM");
            return;
        }

        // Step 3: Attempt automated remediation using AI (API call that might fail)
        JsonNode remediationResult = attemptRemediation(diagnosticInfo, systemName, alertType);
        boolean remediationSuccess = remediationResult.get("success").asBoolean();
        String remediationAction = remediationResult.get("action").asText();

        System.out.println("AI attempted remediation: " + remediationAction);

        if (remediationSuccess) {
            System.out.println("Remediation successful!");
            notifyStatus(alertId, systemName, "FIXED", remediationAction);
            return;
        }

        // Step 4: Escalate to appropriate engineer (API call that might fail)
        String engineerId = escalateToEngineer(alertId, systemName, diagnosticInfo, alertType, severity);
        System.out.println("Escalated to engineer: " + engineerId);

        // Step 5: Send Slack alert (API call that might fail)
        sendSlackAlert(engineerId, alertId, systemName, diagnosticInfo, remediationAction);

        // Step 6: Log status (API call that might fail)
        notifyStatus(alertId, systemName, "ESCALATED", "Assigned to " + engineerId);

        System.out.println("Incident response workflow completed");
    }

    private JsonNode diagnoseIncident(String alertId, String systemName, String alertType, String severity)
            throws Exception {
        // Simulate API failure ~40% of the time
        if (random.nextDouble() < 0.4) {
            throw new Exception("API failure: LLM service unavailable for diagnosis");
        }

        // Simulate LLM analyzing logs and metrics
        ObjectNode result = objectMapper.createObjectNode();
        result.put("alertId", alertId);
        result.put("systemName", systemName);
        result.put("alertType", alertType);
        result.put("severity", severity);
        result.put("timestamp", System.currentTimeMillis());

        // Add specific diagnostic information based on alert type
        if (alertType.equals("CPU_SPIKE")) {
            result.put("cpuUsage", 95.5);
            result.put("processName", "java");
            result.put("pid", 12345);
            result.put("threadCount", 156);
            result.put("diagnosticSummary", "High CPU usage detected in Java process. Possible infinite loop.");
        } else if (alertType.equals("MEMORY_LEAK")) {
            result.put("memoryUsage", 87.2);
            result.put("heapSize", 4096);
            result.put("gcTime", 450);
            result.put("leakingSuspects", "CacheManager, FileHandler");
            result.put("diagnosticSummary", "Memory leak detected. Heap analysis shows accumulation in object caches.");
        } else if (alertType.equals("API_LATENCY")) {
            result.put("p99Latency", 2300);
            result.put("errorRate", 5.2);
            result.put("endpoint", "/api/v1/data");
            result.put("databaseLatency", 1800);
            result.put("diagnosticSummary", "High API latency. Database queries are taking longer than usual.");
        }

        System.out.println("Successfully diagnosed incident");
        return result;
    }

    private boolean validateIncident(JsonNode diagnosticInfo, String alertType) throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Validation service unavailable");
        }

        // Simple validation logic with some randomization to simulate false alarms
        boolean isRealIncident = true;

        // Randomly mark some incidents as false alarms
        if (random.nextInt(10) == 0) {
            isRealIncident = false;
        }

        System.out.println("Incident validation result: " + (isRealIncident ? "REAL INCIDENT" : "FALSE ALARM"));
        return isRealIncident;
    }

    private JsonNode attemptRemediation(JsonNode diagnosticInfo, String systemName, String alertType) throws Exception {
        // Simulate API failure ~40% of the time
        if (random.nextDouble() < 0.4) {
            throw new Exception("API failure: Remediation service unavailable");
        }

        // Create response object
        ObjectNode result = objectMapper.createObjectNode();

        // Determine remediation action based on alert type
        String remediationAction = "Restart service " + systemName;

        if (alertType.equals("CPU_SPIKE")) {
            remediationAction = "Restart JVM with adjusted thread pool settings";
        } else if (alertType.equals("MEMORY_LEAK")) {
            remediationAction = "Clear object cache and restart service";
        } else if (alertType.equals("API_LATENCY")) {
            remediationAction = "Flush DB connection pool and restart API gateway";
        }

        result.put("action", remediationAction);

        // 50% success rate for demonstration
        boolean success = random.nextBoolean();
        result.put("success", success);

        return result;
    }

    private String escalateToEngineer(
            String alertId, String systemName, JsonNode diagnosticInfo, String alertType, String severity)
            throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Engineer assignment service unavailable");
        }

        // Simple logic to determine engineer
        String engineerId;

        if (alertType.equals("CPU_SPIKE")) {
            engineerId = "performance-team-1";
        } else if (alertType.equals("MEMORY_LEAK")) {
            engineerId = "memory-expert-1";
        } else if (alertType.equals("API_LATENCY")) {
            engineerId = "api-team-2";
        } else {
            engineerId = "support-team-1";
        }

        return engineerId;
    }

    private void sendSlackAlert(
            String engineerId, String alertId, String systemName, JsonNode diagnosticInfo, String remediationAttempted)
            throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Slack notification service unavailable");
        }

        System.out.println("Sent Slack alert to engineer " + engineerId + " about alert " + alertId);
    }

    private void notifyStatus(String alertId, String systemName, String status) throws Exception {
        notifyStatus(alertId, systemName, status, null);
    }

    private void notifyStatus(String alertId, String systemName, String status, String details) throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Notification service unavailable");
        }

        System.out.println(
                "Logged status update for alert " + alertId + " on system " + systemName + " with status: " + status);
    }
}
