package lh.it.demo;

import java.io.IOException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

public class App {

    // Define constants for task and event names
    private static final String VERIFY_TASK = "verify-incident";
    private static final String PERIODIC_CHECK_TASK = "periodic-check-task";
    private static final String SEND_CRITICAL_ALERT_TASK = "send-critical-alert";
    private static final String EXTERNAL_EVENT_NAME = "incident-resolved";

    // Method to register an external event in the LittleHorse workflow system
    private static void registerExternalEventDef(LittleHorseBlockingStub client) {
        System.out.println("Registering external event " + EXTERNAL_EVENT_NAME);

            // Create a new external event definition
            client.putExternalEventDef(
                    PutExternalEventDefRequest.newBuilder().setName(EXTERNAL_EVENT_NAME).build()
            );
            System.out.println("External event registered successfully.");
    }

    // Method to register workflow and task definitions
    private static void registerWorkflow() throws IOException {
        LHConfig config = new LHConfig();

        // Create task worker instances for each task
        IncidentWorker workerInstance = new IncidentWorker();
        LHTaskWorker verifyTaskWorker = new LHTaskWorker(workerInstance, VERIFY_TASK, config);
        LHTaskWorker periodicCheckTaskWorker = new LHTaskWorker(workerInstance, PERIODIC_CHECK_TASK, config);
        LHTaskWorker sendCriticalAlertTaskWorker = new LHTaskWorker(workerInstance, SEND_CRITICAL_ALERT_TASK, config);

        // Register each task definition
        verifyTaskWorker.registerTaskDef();
        periodicCheckTaskWorker.registerTaskDef();
        sendCriticalAlertTaskWorker.registerTaskDef();

        // Register the workflow specification
        ConditionalsExample conditionalsExample = new ConditionalsExample();
        conditionalsExample.getWorkflow().registerWfSpec(config.getBlockingStub());

        System.out.println("Workflow registered successfully.");
    }

    // Method to start an individual task worker
    private static void startTaskWorker(LHTaskWorker worker) throws IOException {
        // Add a shutdown hook to ensure the worker is closed properly
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        // Start the worker to begin listening for tasks
        worker.start();
        System.out.println("Started worker for task: " + worker.getTaskDefName());
    }

    // Method to start all necessary task workers
    private static void startTaskWorkers() throws IOException {
        LHConfig config = new LHConfig();
        // Start each task worker
        startTaskWorker(new LHTaskWorker(new IncidentWorker(), VERIFY_TASK, config));
        startTaskWorker(new LHTaskWorker(new IncidentWorker(), PERIODIC_CHECK_TASK, config));
        startTaskWorker(new LHTaskWorker(new IncidentWorker(), SEND_CRITICAL_ALERT_TASK, config));
    }

    // Main method to execute the application
    public static void main(String[] args) throws IOException {
        // Validate command line arguments
        if (args.length != 1 || (!args[0].equals("register") && !args[0].equals("start"))) {
            System.err.println("Argument required: 'register' or 'start'");
            System.exit(1);
        }

        // Establish a gRPC channel to communicate with the LittleHorse server
        String host = "localhost";
        int port = 2023;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        LittleHorseBlockingStub client = LittleHorseGrpc.newBlockingStub(channel);

        try {
            // Execute registration or start tasks based on the command line argument
            if (args[0].equals("register")) {
                registerExternalEventDef(client); // Register external event
                registerWorkflow(); // Register workflow and task definitions
            } else {
                startTaskWorkers(); // Start task workers to handle tasks
            }
        } finally {
            // Ensure proper shutdown of the gRPC channel
            channel.shutdown();
        }
    }
}
