package io.littlehorse.tasks;

import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;

@LHTask
public class NotifyCustomerVerifiedTask {
    public static final String NOTIFY_CUSTOMER_VERIFIED_TASK = "notify-customer-verified";

    @LHTaskMethod(NOTIFY_CUSTOMER_VERIFIED_TASK)
    public String notifyCustomerVerified(String fullName, String email) {
        return "Notification sent to customer " + fullName + " that their identity has been verified";
    }
}
