package io.littlehorse.document.processing.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class ValidateDocumentTask {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@LHTaskMethod("validate-document-data")
	public boolean validateDocumentData(String extractedInfoJson, String documentType) {
		return validateDocument(extractedInfoJson, documentType);
	}

	@LHTaskMethod("validate-document")
	public boolean validateDocument(Object extractedInfo, Object documentType) {
		System.out.println(
				"Validating document. Info type: " + (extractedInfo != null ? extractedInfo.getClass().getName() : "null"));
		System.out.println("Document type: " + (documentType != null ? documentType.getClass().getName() : "null"));

		try {
			// Convert inputs to strings depending on their actual type
			String extractedInfoJson;
			String docType;

			if (extractedInfo instanceof String) {
				extractedInfoJson = (String) extractedInfo;
			} else {
				extractedInfoJson = objectMapper.writeValueAsString(extractedInfo);
			}

			if (documentType instanceof String) {
				docType = (String) documentType;
			} else if (documentType != null) {
				docType = documentType.toString();
			} else {
				docType = "UNKNOWN";
			}

			// Proceed with validation as before
			JsonNode data = objectMapper.readTree(extractedInfoJson);

			// Basic validation - make sure required fields exist
			boolean isValid = data.has("documentId") && data.has("documentType");

			// Additional validation based on document type
			if ("INVOICE".equals(docType)) {
				isValid = isValid && data.has("invoiceNumber") && data.has("amount") && data.has("vendor");

				// 15% chance of validation failure for invoices
				if (Math.random() < 0.15) {
					System.out.println("Validation failed: Missing required invoice fields");
					return false;
				}
			} else if ("CONTRACT".equals(docType)) {
				isValid = isValid && data.has("contractNumber") && data.has("partyA") && data.has("partyB");

				// 20% chance of validation failure for contracts
				if (Math.random() < 0.2) {
					System.out.println("Validation failed: Missing required contract fields");
					return false;
				}
			}

			System.out.println("Document validation result: " + (isValid ? "PASSED" : "FAILED"));
			return isValid;

		} catch (Exception e) {
			System.err.println("Error validating document: " + e.getMessage());
			e.printStackTrace();
			// For more robust behavior, returning false instead of throwing
			return false;
		}
	}
}