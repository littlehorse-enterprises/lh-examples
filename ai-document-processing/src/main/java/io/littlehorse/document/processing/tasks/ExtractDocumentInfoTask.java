package io.littlehorse.document.processing.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.littlehorse.document.processing.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Random;

public class ExtractDocumentInfoTask {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_EXTRACT_DOCUMENT_INFO)
    public JsonNode extractDocumentInfo(String documentId, String documentType) throws Exception {

        // Simulate API failure ~40% of the time to represent LLM service unreliability
        if (random.nextDouble() < 0.4) {
            throw new Exception("API failure: LLM service unavailable");
        }

        // Simulate successful LLM extraction
        ObjectNode result = objectMapper.createObjectNode();
        result.put("documentId", documentId);
        result.put("type", documentType);
        result.put("extractedAt", System.currentTimeMillis());

        if (documentType.equals("INVOICE")) {
            result.put("amount", 1250.00);
            result.put("vendor", "Acme Corp");
            result.put("dueDate", "2023-12-15");
            result.put("sku", "1234567890");
            result.put(
                    "summary",
                    "Our Company owes Acme Corp $1250.00 for services rendered on 2023-12-15, for the purchase of 100 widgets.");
        } else if (documentType.equals("CONTRACT")) {
            result.put("partyA", "Our Company");
            result.put("partyB", "Partner Inc");
            result.put("effectiveDate", "2023-10-01");
            result.put("termLength", "12 months");
            result.put(
                    "summary",
                    "Our Company signs the rights to use Partner Inc's intellectual property for 12 months at the cost of $12 USD. Idemnification clauses are indefinite and unlimited. Term of convience for cancellation is 180 days.");
        }

        return result;
    }
}
