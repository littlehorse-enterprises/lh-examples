package io.littlehorse.example;

import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;

@LHTask
public class KnowYourCustomerTasks {

    @LHTaskMethod("fetch-customer")
    public boolean isPlatinum(String customerId) {
        // For the purpose of this demo, only Obi-Wan has platinum status (:
        return customerId.equals("Obi-Wan");
    }

    @LHTaskMethod("send-special-welcome")
    public String sendSpecialWelcome(String customerId) {
        return "Special welcome sent to customer " + customerId;
    }

    @LHTaskMethod("process-arrival")
    public String processArrival(String customerId) {
        return "Processed hotel arrival for customer " + customerId;
    }
}
