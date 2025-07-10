package io.littlehorse.document.processor.tasks;

import java.util.Random;

import io.littlehorse.document.processor.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotifySubmitterTask {
    private final Random random = new Random();

    @LHTaskMethod(LHConstants.TASK_NOTIFY_SUBMITTER)
    public String notifySubmitter(String submitterId, String documentId, String status) throws Exception {

        // Simulate API failure ~33% of the time
        if (random.nextInt(3) == 0) {
            throw new Exception("API failure: Notification service unavailable");
        }

        return "Successfully notified submitter " + submitterId + " about document " + documentId + " with status: "
                + status;
    }
}
