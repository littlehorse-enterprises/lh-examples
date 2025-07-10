package io.littlehorse.notifications;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class Worker implements Closeable {
  private List<LHTaskWorker> workers = new ArrayList<>();
  private static List<String> tasks = List.of("notify-request-passport", "notify-user-verified", "notify-user-rejected", "notify-manual-verification");

  public Worker(LHConfig config, Mailer mailer, String frontendUrl, String manager) {
    Tasks notificationTasks = new Tasks(mailer, frontendUrl, manager);

    for (String task : tasks) {
      workers.add(new LHTaskWorker(notificationTasks, task, config));
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
