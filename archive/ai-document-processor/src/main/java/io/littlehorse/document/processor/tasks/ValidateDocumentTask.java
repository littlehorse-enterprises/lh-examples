package io.littlehorse.document.processor.tasks;

import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;

import io.littlehorse.document.processor.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class ValidateDocumentTask {
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_VALIDATE_DOCUMENT)
    public boolean validateDocument(JsonNode documentInfo, String documentType) throws Exception {

        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Validation service unavailable");
        }

        // Simple validation logic
        boolean isValid = true;
        if (documentType.equals("INVOICE")) {
            isValid = documentInfo.has("amount") && documentInfo.has("dueDate");
        } else if (documentType.equals("CONTRACT")) {
            isValid = documentInfo.has("partyA") && documentInfo.has("partyB") && documentInfo.has("effectiveDate");
        }

        return isValid;
    }
}
