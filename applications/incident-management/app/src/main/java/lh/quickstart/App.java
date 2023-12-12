package lh.quickstart;

import java.io.IOException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

public class App {

    // Task identifiers
    private static final String VERIFY_TASK = "verify-incident";
    private static final String PERIODIC_CHECK_TASK = "periodic-check-task";
    private static final String EXTERNAL_EVENT_NAME = "incident-resolved";

    // Method to register the external event with LittleHorse
    private static void registerExternalEventDef(LHPublicApiBlockingStub client) {
        System.out.println("Registering external event " + EXTERNAL_EVENT_NAME);
        try {
            // Attempt to create a new external event definition
            client.putExternalEventDef(
                    PutExternalEventDefRequest.newBuilder().setName(EXTERNAL_EVENT_NAME).build()
            );
            System.out.println("External event registered successfully.");
        } catch (StatusRuntimeException exn) {
            // Handle the case where the external event already exists
            if (exn.getStatus().getCode() == Code.ALREADY_EXISTS) {
                System.out.println("External event already exists!");
            } else {
                // Re-throw other exceptions to be handled externally
                throw exn;
            }
        }
    }

    // Method to register the workflow and task definitions
    private static void registerWorkflow() throws IOException {
        LHConfig config = new LHConfig();

        // Create instances of the task worker for each task
        IncidentWorker workerInstance = new IncidentWorker();
        LHTaskWorker verifyTaskWorker = new LHTaskWorker(workerInstance, VERIFY_TASK, config);
        LHTaskWorker periodicCheckTaskWorker = new LHTaskWorker(workerInstance, PERIODIC_CHECK_TASK, config);

        // Register task definitions
        verifyTaskWorker.registerTaskDef(true);
        periodicCheckTaskWorker.registerTaskDef(true);

        // Register the workflow specification
        QuickstartWorkflow quickstartWorkflow = new QuickstartWorkflow();
        quickstartWorkflow.getWorkflow().registerWfSpec(config.getBlockingStub());

        System.out.println("Workflow registered successfully.");
    }

    // Method to start a given task worker
    private static void startTaskWorker(LHTaskWorker worker) throws IOException {
        // Add a shutdown hook to close the worker gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        // Start the worker to listen for tasks
        worker.start();
        System.out.println("Started worker for task: " + worker.getTaskDefName());
    }

    // Method to start all necessary task workers
    private static void startTaskWorkers() throws IOException {
        LHConfig config = new LHConfig();
        startTaskWorker(new LHTaskWorker(new IncidentWorker(), VERIFY_TASK, config));
        startTaskWorker(new LHTaskWorker(new IncidentWorker(), PERIODIC_CHECK_TASK, config));
    }

    // Main method to execute the application
    public static void main(String[] args) throws IOException {
        // Ensure the correct arguments are provided
        if (args.length != 1 || (!args[0].equals("register") && !args[0].equals("start"))) {
            System.err.println("Argument required: 'register' or 'start'");
            System.exit(1);
        }

        // Establish a connection to the LittleHorse server
        String host = "localhost";
        int port = 2023;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        LHPublicApiBlockingStub client = LHPublicApiGrpc.newBlockingStub(channel);

        try {
            // Execute the specified command
            if (args[0].equals("register")) {
                registerExternalEventDef(client); // Register the external event
                registerWorkflow(); // Then register the workflow
            } else {
                startTaskWorkers(); // Start task workers
            }
        } finally {
            // Ensure the gRPC channel is shutdown properly
            channel.shutdown();
        }
    }

}
