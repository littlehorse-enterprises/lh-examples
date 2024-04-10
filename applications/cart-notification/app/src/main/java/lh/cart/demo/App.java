package lh.cart.demo;

import java.io.IOException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

public class App {

    // Method to register external event definitions
    private static void registerExternalEventDef(LittleHorseBlockingStub client) {
        // Registering cart-abandoned event
        System.out.println("Registering external event 'cart-abandoned'");
        client.putExternalEventDef(
                PutExternalEventDefRequest.newBuilder().setName("cart-abandoned").build()
        );
        System.out.println("External event 'cart-abandoned' registered successfully.");

        // Registering checkout-completed event
        System.out.println("Registering external event 'checkout-completed'");
        client.putExternalEventDef(
                PutExternalEventDefRequest.newBuilder().setName("checkout-completed").build()
        );
        System.out.println("External event 'checkout-completed' registered successfully.");
    }

    // Method to register tasks and workflow
    private static void registerWorkflow(LHConfig config) throws IOException {
        registerTasks(config);

        CartMonitorWorkflow cartMonitorWorkflow = new CartMonitorWorkflow();
        cartMonitorWorkflow.getWorkflow().registerWfSpec(config.getBlockingStub());
        System.out.println("Cart management workflow registered successfully.");
    }

    // Method to register tasks
    private static void registerTasks(LHConfig config) throws IOException {
        CartMonitorWorker workerInstance = new CartMonitorWorker();
        new LHTaskWorker(workerInstance, "cart-monitor-task", config).registerTaskDef();
        System.out.println("Task 'cart-monitor-task' registered successfully.");

        new LHTaskWorker(workerInstance, "notify-cart", config).registerTaskDef();
        System.out.println("Task 'notify-cart' registered successfully.");
    }

    // Method to start individual task worker
    private static void startTaskWorker(LHTaskWorker worker) {
        try {
            worker.start();
            System.out.println("Started worker for task: " + worker.getTaskDefName());
        } catch (IOException e) {
            System.err.println("Error starting task worker for " + worker.getTaskDefName() + ": " + e.getMessage());
        }
    }

    // Method to start all task workers
    private static void startTaskWorkers() {
        try {
            LHConfig config = new LHConfig();
            startTaskWorker(new LHTaskWorker(new CartMonitorWorker(), "cart-monitor-task", config));
            startTaskWorker(new LHTaskWorker(new CartMonitorWorker(), "notify-cart", config));
        } catch (IOException e) {
            System.err.println("Error starting task workers: " + e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        ManagedChannel channel = null;
        try {
            LHConfig config = new LHConfig();
            channel = ManagedChannelBuilder.forAddress("localhost", 2023).usePlaintext().build();
            LittleHorseBlockingStub client = LittleHorseGrpc.newBlockingStub(channel);

            if (args.length != 1 || (!"register".equals(args[0]) && !"start".equals(args[0]))) {
                System.err.println("Argument required: 'register' or 'start'");
                System.exit(1);
            }

            if ("register".equals(args[0])) {
                registerExternalEventDef(client);
                registerWorkflow(config);
            } else if ("start".equals(args[0])) {
                System.out.println("Starting task workers...");
                startTaskWorkers();
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        } finally {
            if (channel != null && !channel.isShutdown()) {
                try {
                    channel.shutdownNow();
                } catch (Exception e) {
                    System.err.println("Failed to shutdown the channel: " + e.getMessage());
                }
            }
        }
    }
}
