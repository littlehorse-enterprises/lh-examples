package io.littlehorse.ledger.workers;

import java.io.Closeable;
import java.io.IOException;

import io.littlehorse.ledger.NotificationsService;
import io.littlehorse.ledger.tasks.WarehouseTask;
import io.littlehorse.ledger.transaction.TransactionService;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.worker.LHTaskWorker;
import jakarta.annotation.PostConstruct;

public class WarehouseWorker implements Closeable {
  private LHTaskWorker worker;
  private static String taskName = "ship-item";

  public WarehouseWorker(LittleHorseBlockingStub client, LHConfig config, TransactionService transactionService,
      NotificationsService notificationsServices) {
    WarehouseTask tasks = new WarehouseTask(transactionService, notificationsServices);
    this.worker = new LHTaskWorker(tasks, taskName, config);
  }

  @PostConstruct
  public void start() throws IOException {
    worker.registerTaskDef();
    worker.start();
  }

  @Override
  public void close() throws IOException {
    worker.close();
  }

}
