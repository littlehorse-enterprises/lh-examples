package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.Random;

public class KnowYourCustomerTasks {

    private final Random random = new Random();

    @LHTaskMethod("verify-identity")
    public String verifyIdentity(String fullName, String email, @LHType(masked = true) int ssn) {

        if (random.nextDouble() < 0.25) {
            // Simulate an external API failure, throwing 500 status code for example
            throw new RuntimeException("The external identity verification API is down");
        }

        return "Successfully called external API to request verification for " + fullName + " at " + email;
    }

    @LHTaskMethod("notify-customer-verified")
    public String notifyCustomerVerified(String fullName, String email) {
        return "Notification sent to customer " + fullName + " at " + email + " that their identity has been verified";
    }

    @LHTaskMethod("notify-customer-not-verified")
    public String notifyCustomerNotVerified(String fullName, String email) {
        return "Notification sent to customer " + fullName + " at " + email
                + " that their identity has not been verified";
    }
}
