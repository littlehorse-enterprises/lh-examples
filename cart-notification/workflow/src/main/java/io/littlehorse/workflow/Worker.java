package io.littlehorse.workflow;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.worker.LHTaskWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class Worker implements Closeable {
  private List<LHTaskWorker> workers = new ArrayList<>();
  private static List<String> tasks = List.of("notify-cart", "notify-add-to-cart", "notify-checkout");

  public Worker(LittleHorseBlockingStub client, LHConfig config, NotificationsService notificationsServices) {
    Task task = new Task(notificationsServices);

    for (String taskName : tasks) {
      workers.add(new LHTaskWorker(task, taskName, config));
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
