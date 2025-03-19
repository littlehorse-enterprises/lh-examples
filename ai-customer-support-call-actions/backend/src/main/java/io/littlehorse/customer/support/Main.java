package io.littlehorse.customer.support;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class Main {
    static LHConfig config = new LHConfig();

    static MyTasks tasks = new MyTasks();

    static LHTaskWorker transcribeCallWorker = new LHTaskWorker(tasks, LHConstants.TRANSCRIBE_CALL_TASK, config);
    static LHTaskWorker determineActionsWorker = new LHTaskWorker(tasks, LHConstants.DETERMINE_ACTIONS_TASK, config);
    static LHTaskWorker runWorkflowsWorker = new LHTaskWorker(tasks, LHConstants.RUN_WORKFLOWS_TASK, config);

    public static void main(String[] args) {
        registerMetadata();
        startTaskWorkers();
    }

    public static void registerMetadata() {
        // Because we don't start the worker we can suppress the warning
        transcribeCallWorker.registerTaskDef();
        determineActionsWorker.registerTaskDef();
        runWorkflowsWorker.registerTaskDef();
        // Since we didn't start the worker, this is a no-op, but it prevents
        // VSCode from underlining with a squiggly
        transcribeCallWorker.close();
        determineActionsWorker.close();
        runWorkflowsWorker.close();

        CustomerSupportCallActionsWorkflow myWorkflow = new CustomerSupportCallActionsWorkflow();
        myWorkflow.getWorkflow().registerWfSpec(config.getBlockingStub());
    }

    public static void startTaskWorkers() {
        // Close the worker upon shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(transcribeCallWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(determineActionsWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(runWorkflowsWorker::close));

        System.out.println("Starting task workers!");
        transcribeCallWorker.start();
        determineActionsWorker.start();
        runWorkflowsWorker.start();
    }
}