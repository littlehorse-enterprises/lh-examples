package io.littlehorse.incident.responder.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.littlehorse.incident.responder.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Random;

public class DiagnoseIncidentTask {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_DIAGNOSE_INCIDENT)
    public JsonNode diagnoseIncident(String alertId, String systemName, String alertType, String severity)
            throws Exception {
        // Simulate API failure ~40% of the time to represent LLM service unreliability
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
            result.put("memoryUsage", 3.2);
            result.put(
                    "diagnosticSummary",
                    "High CPU usage detected in Java process. Analysis suggests a potential infinite loop or memory leak in service "
                            + systemName);
        } else if (alertType.equals("MEMORY_LEAK")) {
            result.put("memoryUsage", 87.2);
            result.put("heapSize", 4096);
            result.put("gcTime", 450);
            result.put("leakingSuspects", "CacheManager, FileHandler");
            result.put(
                    "diagnosticSummary",
                    "Memory leak detected in service " + systemName
                            + ". Heap analysis shows accumulation in object caches. Recommend checking CacheManager implementation.");
        } else if (alertType.equals("API_LATENCY")) {
            result.put("p99Latency", 2300);
            result.put("errorRate", 5.2);
            result.put("endpoint", "/api/v1/data");
            result.put("databaseLatency", 1800);
            result.put("activeConnections", 95);
            result.put(
                    "diagnosticSummary",
                    "High API latency in service " + systemName
                            + ". Database queries are taking longer than usual, possibly due to missing index or connection pool saturation.");
        } else {
            result.put(
                    "diagnosticSummary",
                    "Alert detected in service " + systemName + ". Detailed analysis unavailable for alert type "
                            + alertType);
        }

        // Simulate thinking time of an LLM
        Thread.sleep(800);

        return result;
    }
}
