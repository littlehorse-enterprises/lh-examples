package io.littlehorse.verifications;

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
  private static List<String> tasks = List.of("process-passport");

  public Worker(LittleHorseBlockingStub client, LHConfig config) {
    Tasks verificationTasks = new Tasks();

    for (String task : tasks) {
      workers.add(new LHTaskWorker(verificationTasks, task, config));
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
