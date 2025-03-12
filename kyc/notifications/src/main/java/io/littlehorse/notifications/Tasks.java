package io.littlehorse.notifications;

import java.io.IOException;

import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class Tasks {
  private Mailer mailer;
  private String frontendUrl;
  private String manager;

  @LHTaskMethod("notify-request-passport")
  public void requestPassport(User user, WorkerContext context) throws LHTaskException {
    String subject = "Please submit your passport";
    String wfRunId = context.getWfRunId().getId();
    String content = String.format(
        "Hello %s, please submit your passport to proceed with the verification %s/verification/%s",
        user.getFirstname(),
        this.frontendUrl, wfRunId);

    log.info(String.format("Frontend URL: %s", this.frontendUrl));
    try {
      mailer.send(user.getEmail(), subject, content);
    } catch (IOException e) {
      log.error("Failed to send email");
      throw new LHTaskException("notify-request-passport-failed", e.getMessage());
    }
  }

  @LHTaskMethod("notify-user-rejected")
  public void userRejected(User user) throws IOException {
    String subject = "Your account was rejected";
    String content = String.format("Hello %s, we were unable to validate your identity",
        user.getFirstname());

    mailer.send(user.getEmail(), subject, content);
  }

  @LHTaskMethod("notify-user-verified")
  public void userVerified(User user) throws IOException {
    String subject = "Your account was verified";
    String content = String.format("Hello %s, you have passed the KYC successfully",
        user.getFirstname());

    mailer.send(user.getEmail(), subject, content);
  }

  @LHTaskMethod("notify-manual-verification")
  public void manualValidation(User user) throws IOException {
    String subject = "Verify user passport";
    String content = String.format(
        "A new user passport was not verified automatically: %s %s, please verify at %s/admin", user.getFirstname(),
        user.getLastname(), this.frontendUrl);

    mailer.send(manager, subject, content);
  }

}
