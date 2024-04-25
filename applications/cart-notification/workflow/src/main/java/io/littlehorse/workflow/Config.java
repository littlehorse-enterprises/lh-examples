package io.littlehorse.workflow;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

/**
 * Class responsible for loading configuration of the server from environment
 * variables.
 */
@SpringBootConfiguration
public class Config {

  @Bean
  public LHConfig lhConfig() {
    return new LHConfig();
  }

  @Bean
  public LittleHorseGrpc.LittleHorseBlockingStub lhClient(LHConfig config) throws Exception {
    return config.getBlockingStub();
  }

  @Bean
  public Worker warehouseWorker(LittleHorseBlockingStub client, LHConfig config, NotificationsService notificationsServices) {
    return new Worker(client, config, notificationsServices);
  }

  @Bean
  public CartWorkflow registerWorkflow(LittleHorseBlockingStub client) {
    return new CartWorkflow(client);
  }

}
