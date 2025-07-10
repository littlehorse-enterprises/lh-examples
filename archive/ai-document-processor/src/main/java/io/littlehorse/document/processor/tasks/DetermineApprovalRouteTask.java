package io.littlehorse.document.processor.tasks;

import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;

import io.littlehorse.document.processor.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class DetermineApprovalRouteTask {
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_DETERMINE_APPROVAL_ROUTE)
    public String determineApprovalRoute(JsonNode documentInfo, String documentType) throws Exception {

        // Simulate API failure ~40% of the time to represent LLM service unreliability
        if (random.nextDouble() < 0.4) {
            throw new Exception("API failure: LLM service unavailable");
        }

        // Simulate LLM decision making based on document type and content
        String department;

        if (documentType.equals("INVOICE")) {
            // For invoices, route based on the amount if present
            if (documentInfo.has("amount")) {
                double amount = documentInfo.get("amount").asDouble();
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
            if (documentInfo.has("termLength")) {
                String termLength = documentInfo.get("termLength").asText();
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
            department = "HR";
        }

        // Simulate thinking time of an LLM
        Thread.sleep(500);

        return department;
    }
}
