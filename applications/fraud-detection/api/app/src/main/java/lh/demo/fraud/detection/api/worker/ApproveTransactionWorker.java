package lh.demo.fraud.detection.api.worker;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;
import jakarta.annotation.PostConstruct;
import java.io.Closeable;
import lh.demo.fraud.detection.api.TransactionRepository;
import lh.demo.fraud.detection.api.task.TransactionTasks;

public class ApproveTransactionWorker implements Closeable {
    private final LHTaskWorker worker;

    public ApproveTransactionWorker(LHConfig config, TransactionRepository repository) {
        TransactionTasks tasks = new TransactionTasks(repository);
        this.worker = new LHTaskWorker(tasks, TransactionTasks.APPROVE_TRANSACTION, config);
    }

    @PostConstruct
    public void start() {
        worker.registerTaskDef();
        worker.start();
    }

    @Override
    public void close() {
        worker.close();
    }
}
