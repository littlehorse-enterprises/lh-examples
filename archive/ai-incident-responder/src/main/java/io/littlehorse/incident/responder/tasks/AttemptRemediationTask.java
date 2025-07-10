package io.littlehorse.incident.responder.tasks;

import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.littlehorse.incident.responder.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class AttemptRemediationTask {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Random random = new Random();

	@LHTaskMethod(LHConstants.TASK_ATTEMPT_REMEDIATION)
	public JsonNode attemptRemediation(JsonNode diagnosticInfo, String systemName, String alertType) throws Exception {
		// Simulate API failure ~40% of the time to represent LLM service unreliability
		if (random.nextDouble() < 0.4) {
			throw new Exception("API failure: Remediation service unavailable");
		}

		// Create response object
		ObjectNode result = objectMapper.createObjectNode();

		// Determine remediation action based on alert type and system
		String remediationAction = determineRemediationAction(alertType, diagnosticInfo, systemName);
		result.put("action", remediationAction);

		// Simulate remediation success/failure
		// For demo purposes, remediation has a 50% chance of success
		boolean success = random.nextBoolean();
		result.put("success", success);

		// Add details to the result
		if (success) {
			result.put("message", "Successfully remediated issue with action: " + remediationAction);
		} else {
			result.put("message", "Attempted remediation with action: " + remediationAction + " but was unsuccessful");
			result.put("failureReason", generateFailureReason(alertType));
		}

		// Simulate thinking and execution time
		Thread.sleep(1000);

		return result;
	}

	private String determineRemediationAction(String alertType, JsonNode diagnosticInfo, String systemName) {
		// Determine appropriate remediation based on alert type and diagnostics
		if (alertType.equals("CPU_SPIKE")) {
			if (diagnosticInfo.has("processName") && "java".equals(diagnosticInfo.get("processName").asText())) {
				if (diagnosticInfo.has("threadCount") && diagnosticInfo.get("threadCount").asInt() > 100) {
					return "Restart JVM with adjusted thread pool settings";
				} else {
					return "Initiate GC and heap dump for analysis";
				}
			} else {
				return "Restart affected service " + systemName;
			}
		} else if (alertType.equals("MEMORY_LEAK")) {
			if (diagnosticInfo.has("leakingSuspects")) {
				String suspects = diagnosticInfo.get("leakingSuspects").asText();
				if (suspects.contains("CacheManager")) {
					return "Clear object cache and adjust cache TTL";
				} else if (suspects.contains("FileHandler")) {
					return "Close leaking file handles and restart service";
				}
			}
			return "Restart service " + systemName + " with increased heap size";
		} else if (alertType.equals("API_LATENCY")) {
			if (diagnosticInfo.has("databaseLatency") && diagnosticInfo.get("databaseLatency").asInt() > 1000) {
				return "Flush DB connection pool and analyze slow queries";
			} else if (diagnosticInfo.has("activeConnections") && diagnosticInfo.get("activeConnections").asInt() > 80) {
				return "Scale up service " + systemName + " to handle increased load";
			}
			return "Restart API gateway and clear API cache";
		} else {
			return "Restart affected service " + systemName;
		}
	}

	private String generateFailureReason(String alertType) {
		String[] failureReasons = {
				"Service could not be restarted - permission denied",
				"Failed to clear cache - cache manager unreachable",
				"Unable to scale service - orchestration platform error",
				"DB connection pool reset failed - DB unresponsive",
				"JVM heap settings could not be adjusted - insufficient memory",
				"Service restart timed out after 30 seconds",
				"Could not initiate GC - JVM not responding to commands"
		};

		return failureReasons[random.nextInt(failureReasons.length)];
	}
}