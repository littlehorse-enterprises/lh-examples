package io.littlehorse.document.processing.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class DetermineApprovalRouteTask extends BaseApiTask {
	private final ObjectMapper objectMapper = new ObjectMapper();

	public DetermineApprovalRouteTask() {
		// Higher failure rate for LLM API to simulate reliability issues
		super(0.4);
	}

	@LHTaskMethod("determine-approval-route")
	public String determineApprovalRoute(String extractedInfoJson, String documentType) throws Exception {
		// Simulate API call that might fail
		simulateApiCall("LLM service");

		// Parse the extracted information
		JsonNode extractedInfo = objectMapper.readTree(extractedInfoJson);

		// Simulate LLM decision making based on document type and content
		String department;

		if (documentType.equals("INVOICE")) {
			// For invoices, route based on the amount if present
			if (extractedInfo.has("amount")) {
				double amount = extractedInfo.get("amount").asDouble();
				if (amount > 1000) {
					department = "FINANCE";
				} else {
					department = "HR";
				}
			} else {
				department = "FINANCE";
			}
		} else if (documentType.equals("CONTRACT")) {
			// For contracts, check if specific terms are present
			if (extractedInfo.has("termLength")) {
				String termLength = extractedInfo.get("termLength").asText();
				if (termLength.contains("12")) {
					department = "LEGAL";
				} else {
					department = "HR";
				}
			} else {
				department = "LEGAL";
			}
		} else {
			// Default department for unknown document types
			department = "LEGAL";
		}

		// Simulate thinking time of an LLM
		Thread.sleep(500);

		return department;
	}
}