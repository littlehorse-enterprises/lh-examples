package io.littlehorse.notifications;

import java.io.IOException;

public interface Mailer {
  public void send(String to, String subject, String content) throws IOException;
}
