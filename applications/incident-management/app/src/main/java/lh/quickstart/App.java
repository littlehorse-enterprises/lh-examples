package lh.quickstart;

import java.io.IOException;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class App {

    private static final String VERIFY_TASK = "verify-incident";
    private static final String PERIODIC_CHECK_TASK = "periodic-check-task"; // Task for periodic checks

    private static void registerWorkflow() throws IOException {
        LHConfig config = new LHConfig();

        // Create an instance of the task worker
        IncidentWorker workerInstance = new IncidentWorker();

        // Register the 'verify-incident' task definition
        LHTaskWorker verifyTaskWorker = new LHTaskWorker(workerInstance, VERIFY_TASK, config);
        verifyTaskWorker.registerTaskDef(true);

        // Register the 'periodic-check-task' task definition
        LHTaskWorker periodicCheckTaskWorker = new LHTaskWorker(workerInstance, PERIODIC_CHECK_TASK, config);
        periodicCheckTaskWorker.registerTaskDef(true);

        // Register the workflow
        QuickstartWorkflow quickstartWorkflow = new QuickstartWorkflow();
        quickstartWorkflow.getWorkflow().registerWfSpec(config.getBlockingStub());

        System.out.println("Workflow registered successfully.");
    }

    private static void startWorkflow() throws IOException {
        LHConfig config = new LHConfig();

        // Start the task worker for 'verify-incident'
        LHTaskWorker verifyTaskWorker = new LHTaskWorker(new IncidentWorker(), VERIFY_TASK, config);
        startTaskWorker(verifyTaskWorker);

        // Start the task worker for 'periodic-check-task'
        LHTaskWorker periodicCheckTaskWorker = new LHTaskWorker(new IncidentWorker(), PERIODIC_CHECK_TASK, config);
        startTaskWorker(periodicCheckTaskWorker);

        System.out.println("Workers started for handling incident tasks.");
    }

    // Helper method to start and register task workers
    private static void startTaskWorker(LHTaskWorker worker) throws IOException {
        // Ensure the worker is properly closed during shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));

        // Start the worker to listen for tasks
        worker.start();
        System.out.println("Started worker for task: " + worker.getTaskDefName());
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1 || (!args[0].equals("register") && !args[0].equals("start"))) {
            System.err.println("Argument required: 'register' or 'start'");
            System.exit(1);
        }

        if (args[0].equals("register")) {
            registerWorkflow();
        } else {
            startWorkflow();
        }
    }
}
