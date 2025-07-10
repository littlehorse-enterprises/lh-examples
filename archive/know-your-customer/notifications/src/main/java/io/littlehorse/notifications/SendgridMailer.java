package io.littlehorse.notifications;

import java.io.IOException;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class SendgridMailer implements Mailer {
  private SendGrid sendgridClient;
  private String from;

  @Override
  public void send(String to, String subject, String content) throws IOException {
    Mail mail = newMail(to, subject, content);
    Request request = newRequest(mail);
    log.info(String.format("Sending email: %s", subject));
    sendgridClient.api(request);
  }

  private Mail newMail(String to, String subject, String content) {
    return new Mail(new Email(this.from), subject, new Email(to), new Content("text/plain", content));
  }

  private Request newRequest(Mail mail) throws IOException {
    Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());
    return request;
  }

}
