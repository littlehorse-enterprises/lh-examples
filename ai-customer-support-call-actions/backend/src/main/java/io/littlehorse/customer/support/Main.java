package io.littlehorse.customer.support;

import java.util.List;

import io.littlehorse.customer.support.workflows.CustomerSupportCallActionsWorkflow;
import io.littlehorse.customer.support.workflows.EscalateCaseWorkflow;
import io.littlehorse.customer.support.workflows.SendEmailWorkflow;
import io.littlehorse.customer.support.workflows.UpdateCustomerNotesWorkflow;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class Main {
    static LHConfig config = new LHConfig();

    static MyTasks tasks = new MyTasks();

    static List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(tasks, LHConstants.TRANSCRIBE_CALL_TASK, config),
            new LHTaskWorker(tasks, LHConstants.DETERMINE_ACTIONS_TASK, config),
            new LHTaskWorker(tasks, LHConstants.RUN_WORKFLOWS_TASK, config),
            new LHTaskWorker(tasks, LHConstants.ADD_NOTE_TO_CUSTOMER_TASK, config),
            new LHTaskWorker(tasks, LHConstants.ESCALATE_CASE_TASK, config),
            new LHTaskWorker(tasks, LHConstants.SEND_EMAIL_TASK, config),
            new LHTaskWorker(tasks, LHConstants.AUDIT_LOG_TASK, config),
            new LHTaskWorker(tasks, LHConstants.ANALYZE_CUSTOMER_NOTE_TASK, config),
            new LHTaskWorker(tasks, LHConstants.REDACT_SENSITIVE_INFO_TASK, config),
            new LHTaskWorker(tasks, LHConstants.SCHEDULE_FOLLOW_UP_TASK, config));

    public static void main(String[] args) {
        deleteAllTaskDefs();
        registerMetadata();
        startTaskWorkers();
    }

    public static void deleteAllTaskDefs() {
        for (LHTaskWorker worker : workers) {
            if (worker.doesTaskDefExist())
                config.getBlockingStub()
                        .deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                                .setId(TaskDefId.newBuilder()
                                        .setName(worker.getTaskDefName())
                                        .build())
                                .build());
        }
    }

    public static void registerMetadata() {
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register workflows
        new CustomerSupportCallActionsWorkflow().getWorkflow().registerWfSpec(config.getBlockingStub());
        new UpdateCustomerNotesWorkflow().getWorkflow().registerWfSpec(config.getBlockingStub());
        new EscalateCaseWorkflow().getWorkflow().registerWfSpec(config.getBlockingStub());
        new SendEmailWorkflow().getWorkflow().registerWfSpec(config.getBlockingStub());
    }

    public static void startTaskWorkers() {
        System.out.println("Starting task workers!");

        for (LHTaskWorker worker : workers) {
            // Add shutdown hooks for all workers
            Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
            worker.start();
        }
    }
}
