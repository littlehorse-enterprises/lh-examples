package io.littlehorse.document.processing.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class ExtractDocumentInfoTask extends BaseApiTask {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@LHTaskMethod("extract-document-data")
	public String extractDocumentInfo(String documentId, String documentType) {
		System.out.println("Extracting document information for: " + documentId + " of type: " + documentType);

		try {
			// Simulate API call that might fail
			simulateApiCall("Document extraction");

			// Simulate document extraction
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

			// 10% chance of failing with an exception to demonstrate error handling
			if (Math.random() < 0.1) {
				throw new RuntimeException("Failed to extract document info due to poor image quality");
			}

			String extractedInfo = objectMapper.writeValueAsString(result);
			System.out.println("Successfully extracted document info: " + extractedInfo);
			return extractedInfo;

		} catch (Exception e) {
			System.err.println("Error extracting document info: " + e.getMessage());
			throw new RuntimeException("Document extraction failed: " + e.getMessage(), e);
		}
	}
}