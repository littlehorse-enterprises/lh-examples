package io.littlehorse.tasks;

import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.Random;

@LHTask
public class VerifyIdentityTask {
    public static final String VERIFY_IDENTITY_TASK = "verify-identity";

    private final Random random = new Random();

    @LHTaskMethod(VERIFY_IDENTITY_TASK)
    public String verifyIdentity(String fullName, String email, @LHType(masked = true) int ssn) {
        if (random.nextDouble() < 0.25) {
            // Simulate an external API failure, throwing 500 status code for example
            throw new RuntimeException("The external identity verification API is down");
        }

        return "Successfully called external API to request verification for " + fullName;
    }
}
