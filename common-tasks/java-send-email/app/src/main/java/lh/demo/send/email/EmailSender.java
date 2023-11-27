package lh.demo.send.email;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a basic implementation of a Task Worker which uses the SendGrid
 * Java API to send emails.
 */
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    public static final String SEND_EMAIL_TASK = "send-email";

    private SendGrid sendGridClient;
    private String fromEmail;

    public EmailSender(SendGrid sendgrid, String fromEmail) {
        if (sendgrid == null) {
            log.warn("The SendGrid Client object provided was empty; running in 'dummy mode'.");
        } else {
            if (fromEmail == null) {
                throw new IllegalArgumentException(
                        "If you provide a SendGrid object, you must also provide a 'from' email");
            }
            this.sendGridClient = sendgrid;
            this.fromEmail = fromEmail;
            log.info("Running in active mode.");
        }
    }

    @LHTaskMethod(SEND_EMAIL_TASK)
    public String sendEmail(String toAddress, String subject, String body) throws IOException {

        if (sendGridClient == null) {
            log.info("Executing task in dummy mode");
            return "Since there was no API key configured, the worker did not send email."
                    + " It would have sent '%s' to '%s' with subject '%s'.".formatted(body, toAddress, body);
        }

        Mail email =
                new Mail(new Email(this.fromEmail), subject, new Email(toAddress), new Content("text/plain", body));

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(email.build());

        log.info("Sending email now");
        Response response = sendGridClient.api(request);
        boolean successfullySentEmail = response.getStatusCode() >= 200 && response.getStatusCode() < 300;

        if (successfullySentEmail) {
            return "Successfully sent email to %s".formatted(toAddress);
        }

        throw new RuntimeException("Failed to send the email: %s".formatted(response.getBody()));
    }
}
