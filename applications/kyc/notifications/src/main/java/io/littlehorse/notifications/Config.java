package io.littlehorse.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.sendgrid.SendGrid;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;

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
  public Worker worker(LHConfig config, Mailer mailer, @Value("${frontend-url}") String frontendUrl, @Value("${manager}") String manager) {
    return new Worker(config, mailer, frontendUrl, manager);
  }

  @Bean
  @ConditionalOnProperty(value = "sendgrid.enabled", havingValue = "true")
  public Mailer mailer(@Value("${sendgrid.api-key}") String apiKey, @Value("${sendgrid.sender}") String sender) {
    SendGrid sendGridClient = new SendGrid(apiKey);
    return new SendgridMailer(sendGridClient, sender);
  }

  @Bean
  @ConditionalOnProperty(value = "sendgrid.enabled", havingValue = "false", matchIfMissing = true)
  public Mailer voidMailer() {
    return new VoidMailer();
  }
}
