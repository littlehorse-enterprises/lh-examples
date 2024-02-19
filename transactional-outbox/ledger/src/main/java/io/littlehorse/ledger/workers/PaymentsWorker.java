package io.littlehorse.ledger.workers;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.littlehorse.ledger.NotificationsService;
import io.littlehorse.ledger.tasks.PaymentsTask;
import io.littlehorse.ledger.transaction.TransactionService;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.worker.LHTaskWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class PaymentsWorker implements Closeable {
  private List<LHTaskWorker> workers = new ArrayList<>();
  private static List<String> tasks = List.of("process-payment", "issue-refund");

  public PaymentsWorker(LittleHorseBlockingStub client, LHConfig config, TransactionService transactionService,
      NotificationsService notificationsServices) {
    PaymentsTask paymentTasks = new PaymentsTask(transactionService, notificationsServices);

    for (String task : tasks) {
      workers.add(new LHTaskWorker(paymentTasks, task, config));
    }
  }

  @PostConstruct
  public void start() throws IOException {
    for (LHTaskWorker worker : workers) {
      worker.registerTaskDef();
      worker.start();
    }
  }

  @PreDestroy
  @Override
  public void close() throws IOException {
    for (LHTaskWorker worker : workers) {
      worker.close();
    }
  }

}
