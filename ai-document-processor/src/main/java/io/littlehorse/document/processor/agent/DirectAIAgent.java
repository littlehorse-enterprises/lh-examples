package io.littlehorse.document.processor.agent;

import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class demonstrates an AI agent that directly calls tools and APIs
 * without proper orchestration, showing vulnerability to failures.
 */
public class DirectAIAgent {
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public DirectAIAgent() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Process a document without proper orchestration or failure handling
     */
    public void processDocument(String documentId, String documentType, String submitterId) {
        try {
            System.out.println("Starting document processing for document: " + documentId);

            // Step 1: Extract document info (API call that might fail)
            JsonNode extractedInfo = extractDocumentInfo(documentId, documentType);

            // Step 2: Validate document (API call that might fail)
            boolean isValid = validateDocument(extractedInfo, documentType);

            if (!isValid) {
                System.out.println("Document is invalid. Notifying submitter.");
                notifySubmitter(submitterId, documentId, "INVALID");
                return;
            }

            // Step 3: Determine approval route using simulated LLM (API call that might
            // fail)
            String approvalRoute = determineApprovalRoute(extractedInfo, documentType);
            System.out.println("AI determined route: " + approvalRoute);

            // Step 4: Route document to department (API call that might fail)
            routeToAppropriateDepartment(approvalRoute, documentId, extractedInfo);

            // In a real system, we'd wait for approval here
            // But for demonstration, we'll skip ahead

            // Step 5: Notify submitter (API call that might fail)
            notifySubmitter(submitterId, documentId, "PROCESSED");

            System.out.println("Document processing completed successfully");

        } catch (Exception e) {
            System.err.println("ERROR: Document processing failed: " + e.getMessage());
            // The entire process fails with minimal error information
            // No automatic retries or graceful handling
        }
    }

    private JsonNode extractDocumentInfo(String documentId, String documentType) throws Exception {
        // Simulate API failure 40% of the time
        if (random.nextInt(4) == 0) {
            throw new Exception("API failure: LLM service unavailable");
        }

        // Simulate successful extraction
        ObjectNode result = objectMapper.createObjectNode();
        result.put("documentId", documentId);
        result.put("type", documentType);
        result.put("extractedAt", System.currentTimeMillis());

        if (documentType.equals("INVOICE")) {
            result.put("amount", 1250.00);
            result.put("vendor", "Acme Corp");
            result.put("dueDate", "2023-12-15");
        } else if (documentType.equals("CONTRACT")) {
            result.put("partyA", "Our Company");
            result.put("partyB", "Partner Inc");
            result.put("effectiveDate", "2023-10-01");
            result.put("termLength", "12 months");
        }

        System.out.println("Successfully extracted document info");
        return result;
    }

    private boolean validateDocument(JsonNode extractedInfo, String documentType) throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Validation service unavailable");
        }

        // Simple validation logic
        boolean isValid = true;
        if (documentType.equals("INVOICE")) {
            isValid = extractedInfo.has("amount") && extractedInfo.has("dueDate");
        } else if (documentType.equals("CONTRACT")) {
            isValid = extractedInfo.has("partyA") && extractedInfo.has("partyB") && extractedInfo.has("effectiveDate");
        }

        System.out.println("Document validation result: " + (isValid ? "VALID" : "INVALID"));
        return isValid;
    }

    private String determineApprovalRoute(JsonNode extractedInfo, String documentType) throws Exception {
        // Simulate API failure ~40% of the time to represent LLM service unreliability
        if (random.nextDouble() < 0.4) {
            throw new Exception("API failure: LLM service unavailable");
        }

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
            department = "HR";
        }

        // Simulate thinking time of an LLM
        Thread.sleep(500);

        return department;
    }

    private void routeToAppropriateDepartment(String department, String documentId, JsonNode extractedInfo)
            throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Routing service unavailable");
        }

        System.out.println("Successfully routed document to " + department + " department");
    }

    private void notifySubmitter(String submitterId, String documentId, String status) throws Exception {
        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Notification service unavailable");
        }

        System.out.println("Successfully notified submitter " + submitterId + " about document " + documentId
                + " with status: " + status);
    }

    public static void main(String[] args) {
        DirectAIAgent agent = new DirectAIAgent();

        // Process a few documents to demonstrate failures
        for (int i = 0; i < 5; i++) {
            String documentId = "DOC-" + (1000 + i);
            String documentType = i % 2 == 0 ? "INVOICE" : "CONTRACT";
            String submitterId = "USER-" + (100 + i);

            System.out.println("\n========= PROCESSING DOCUMENT " + (i + 1) + " =========");
            try {
                agent.processDocument(documentId, documentType, submitterId);
            } catch (Exception e) {
                System.err.println("Document processing pipeline failed completely");
            }
        }
    }
}
