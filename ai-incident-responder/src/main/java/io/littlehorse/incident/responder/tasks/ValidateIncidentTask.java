package io.littlehorse.incident.responder.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import io.littlehorse.incident.responder.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Random;

public class ValidateIncidentTask {
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_VALIDATE_INCIDENT)
    public boolean validateIncident(JsonNode diagnosticInfo, String alertType) throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Validation service unavailable");
        }

        // Some alerts might be false alarms based on diagnostic data
        boolean isRealIncident = true;

        // For demo purposes, use a mix of logic and randomness to determine if it's a
        // real incident
        if (alertType.equals("CPU_SPIKE") && diagnosticInfo.has("cpuUsage")) {
            double cpuUsage = diagnosticInfo.get("cpuUsage").asDouble();
            // Consider it a false alarm if CPU usage is below 90% and we randomly determine
            // it's transient
            if (cpuUsage < 90.0 && random.nextInt(4) == 0) {
                isRealIncident = false;
            }
        } else if (alertType.equals("MEMORY_LEAK") && diagnosticInfo.has("memoryUsage")) {
            double memoryUsage = diagnosticInfo.get("memoryUsage").asDouble();
            // Consider it a false alarm if memory usage isn't critically high and
            // diagnostics suggest it might be temporary
            if (memoryUsage < 80.0 && random.nextInt(5) == 0) {
                isRealIncident = false;
            }
        } else if (alertType.equals("API_LATENCY") && diagnosticInfo.has("p99Latency")) {
            int latency = diagnosticInfo.get("p99Latency").asInt();
            // Consider it a false alarm if latency isn't critically high and could be just
            // a temporary spike
            if (latency < 2000 && random.nextInt(3) == 0) {
                isRealIncident = false;
            }
        }

        // Randomize some validations to simulate false alarms
        if (random.nextInt(10) == 0) {
            isRealIncident = false;
        }

        return isRealIncident;
    }
}
