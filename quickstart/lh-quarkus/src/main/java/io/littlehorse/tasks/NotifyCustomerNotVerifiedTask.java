package io.littlehorse.tasks;

import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;

@LHTask
public class NotifyCustomerNotVerifiedTask {
    public static final String NOTIFY_CUSTOMER_NOT_VERIFIED_TASK = "notify-customer-not-verified";

    @LHTaskMethod(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK)
    public String notifyCustomerNotVerified(String fullName, String email) {
        return "Notification sent to customer " + fullName + " that their identity has not been verified";
    }
}
