package io.littlehorse.notifications;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class VoidMailer implements Mailer {
  @Override
  public void send(String to, String subject, String content) throws IOException {
    log.info("Sending void email");
    log.info(String.format("To: %s", to));
    log.info(String.format("Subject: %s", subject));
    log.info(String.format("Content: %s", content));
  }
}
