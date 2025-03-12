package io.littlehorse.document.processing.tasks;

import io.littlehorse.document.processing.LHConstants;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Random;

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
