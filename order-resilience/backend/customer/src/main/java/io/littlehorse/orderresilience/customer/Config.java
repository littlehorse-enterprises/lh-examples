package io.littlehorse.orderresilience.customer;

import io.littlehorse.orderresilience.customer.customer.CustomerService;
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
  public Worker worker(LittleHorseBlockingStub client, LHConfig config,
                       CustomerService customerService) {
    return new Worker(client, config, customerService);
  }
}
