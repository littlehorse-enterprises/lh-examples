package lh.cart.demo;

import java.io.IOException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.common.config.LHConfig;

public class CartMonitorWorker {
    @LHTaskMethod("cart-monitor-task")
    public void monitorCart(String cartItemId) {
        // Logic to monitor the cart
        System.out.println("Monitoring cart: " + cartItemId);
        // Trigger notifications or update workflow variables as needed
    }

    @LHTaskMethod("notify-cart")
    public void notifyCart(String message) {
        System.out.println(message);
    }

    public static void startWorker() throws IOException {
        CartMonitorWorker workerInstance = new CartMonitorWorker();
        LHConfig config = new LHConfig();
        LHTaskWorker worker = new LHTaskWorker(workerInstance, "cart-monitor-task", config);
        worker.registerTaskDef();
        worker.start();
    }
}

