package lh.demo.send.email;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import io.littlehorse.sdk.worker.LHTaskMethod;

/**
 * This class is a basic implementation of a Task Worker which uses the SendGrid
 * Java
 * API to send emails.
 */
public class EmailSender {

    Logger log = Logger.getLogger(EmailSender.class.getName());

    public static final String SEND_EMAIL_TASK = "send-email";

    private SendGrid sendGridClient;
    private String fromEmail;

    public EmailSender(Optional<SendGrid> sendgrid, Optional<String> fromEmail) {
        if (sendgrid.isEmpty()) {
            log.warning("The SendGrid Client object provided was empty; running in 'dummy mode'.");
        } else {
            if (fromEmail.isEmpty()) {
                throw new IllegalArgumentException(
                        "If you provide a SendGrid object, you must also provide a 'from' email");
            }
            this.sendGridClient = sendgrid.get();
            this.fromEmail = fromEmail.get();
            log.info("Running in active mode.");
        }
    }

    @LHTaskMethod(SEND_EMAIL_TASK)
    public String sendEmail(String toAddress, String subject, String body)
            throws IOException {

        if (sendGridClient == null) {
            log.info("Executing task in dummy mode");
            return "Since there was no API key configured, the worker did not send email." +
                    " It would have sent '%s' to '%s' with subject '%s'.".formatted(
                            body, toAddress, body);
        }

        Mail email = new Mail(
                new Email(this.fromEmail),
                subject,
                new Email(toAddress),
                new Content("text/plain", body));

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(email.build());

        log.info("Sending email now");
        Response response = sendGridClient.api(request);
        int code = response.getStatusCode();

        boolean ok = code >= 200 && code < 300;
        if (ok) {
            return "Successfully sent email to %s".formatted(toAddress);
        } else {
            throw new RuntimeException("Failed to send the email: %s".formatted(response.getBody()));
        }
    }

}
