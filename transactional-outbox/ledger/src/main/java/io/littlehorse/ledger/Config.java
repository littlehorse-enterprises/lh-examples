package io.littlehorse.ledger;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import io.littlehorse.ledger.transaction.TransactionService;
import io.littlehorse.ledger.workers.PaymentsWorker;
import io.littlehorse.ledger.workers.WarehouseWorker;
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
  @ConditionalOnProperty(value = "worker", havingValue = "warehouse", matchIfMissing = true)
  public WarehouseWorker warehouseWorker(LittleHorseBlockingStub client, LHConfig config,
      TransactionService transactionService, NotificationsService notificationsServices) {
    return new WarehouseWorker(client, config, transactionService, notificationsServices);
  }

  @Bean
  @ConditionalOnProperty(value = "worker", havingValue = "payments", matchIfMissing = false)
  public PaymentsWorker paymentsWorker(LittleHorseBlockingStub client, LHConfig config,
      TransactionService transactionService, NotificationsService notificationsServices) {
    return new PaymentsWorker(client, config, transactionService, notificationsServices);
  }

  @Bean
  @ConditionalOnProperty(value = "register-workflow", havingValue = "true", matchIfMissing = false)
  public OutboxWorkflow registerWorkflow(LittleHorseBlockingStub client) {
    return new OutboxWorkflow(client);
  }

}
