package io.littlehorse.document.processing.tasks;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class RouteToDepartmentTasks extends BaseApiTask {

	@LHTaskMethod("route-to-finance")
	public String routeToFinance(String documentId, String extractedInfoJson) throws Exception {
		// Simulate API call that might fail
		simulateApiCall("Finance routing service");

		return "Successfully routed document " + documentId + " to Finance department";
	}

	@LHTaskMethod("route-to-legal")
	public String routeToLegal(String documentId, String extractedInfoJson) throws Exception {
		// Simulate API call that might fail
		simulateApiCall("Legal routing service");

		return "Successfully routed document " + documentId + " to Legal department";
	}

	@LHTaskMethod("route-to-hr")
	public String routeToHr(String documentId, String extractedInfoJson) throws Exception {
		// Simulate API call that might fail
		simulateApiCall("HR routing service");

		return "Successfully routed document " + documentId + " to HR department";
	}
}