package io.littlehorse.document.processor.tasks;

import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;

import io.littlehorse.document.processor.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class RouteToDeprtmentTask {
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_ROUTE_TO_DEPARTMENT)
    public String routeToDeprtment(String department, String documentId, JsonNode documentInfo) throws Exception {

        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Routing service unavailable");
        }

        return "Successfully routed document to " + department + " department";
    }
}
