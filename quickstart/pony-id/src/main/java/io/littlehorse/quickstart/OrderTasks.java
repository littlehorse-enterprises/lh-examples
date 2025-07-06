package io.littlehorse.quickstart;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class OrderTasks {

    @LHTaskMethod("ship-item")
    public void shipItem(String item) {
        System.out.println(item + " shipped");
    }

    @LHTaskMethod("decline-order")
    public void cancelOrder(String employee, String item) {
        System.out.println("Sorry " + employee + " you have not been approved for " + item);
    }
}
