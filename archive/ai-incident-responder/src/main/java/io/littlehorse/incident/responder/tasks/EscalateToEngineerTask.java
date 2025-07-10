package io.littlehorse.incident.responder.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import io.littlehorse.incident.responder.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Random;

public class EscalateToEngineerTask {
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_ESCALATE_TO_ENGINEER)
    public String escalateToEngineer(
            String alertId, String systemName, JsonNode diagnosticInfo, String alertType, String severity)
            throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Engineer assignment service unavailable");
        }

        // Determine which engineer to assign based on alert type and system
        String engineerId = determineEngineer(systemName, alertType, severity);

        return engineerId;
    }

    private String determineEngineer(String systemName, String alertType, String severity) {
        // In a real system, this would use a more complex algorithm or service
        // For demo purposes, use simple logic based on system and alert type

        if (systemName.contains("db") || systemName.contains("database")) {
            return "db-team-" + (random.nextInt(3) + 1);
        } else if (systemName.contains("api") || alertType.equals("API_LATENCY")) {
            return "api-team-" + (random.nextInt(3) + 1);
        } else if (alertType.equals("MEMORY_LEAK")) {
            return "memory-expert-" + (random.nextInt(2) + 1);
        } else if (alertType.equals("CPU_SPIKE")) {
            return "performance-team-" + (random.nextInt(3) + 1);
        } else if (severity.equals("HIGH") || severity.equals("CRITICAL")) {
            return "sre-oncall-" + (random.nextInt(2) + 1);
        } else {
            return "support-team-" + (random.nextInt(5) + 1);
        }
    }
}
