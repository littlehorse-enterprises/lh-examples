package lh.demo.send.email;

import static lh.demo.send.email.EmailSender.SEND_EMAIL_TASK;

import com.sendgrid.SendGrid;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final String COMMAND_REGISTER = "register";
    private static final String COMMAND_RUN_WORKER = "run-worker";

    /*
     * As per the root README, there are two commands here:
     * - register, which registers the TaskDef
     * - run-worker, which runs the Task Worker.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1 || (!args[0].equals(COMMAND_REGISTER) && !args[0].equals(COMMAND_RUN_WORKER))) {
            log.error("Please provide either the 'register' or 'run-worker' command");
            System.exit(1);
        }

        LHConfig config = new LHConfig();
        SendGrid client = Optional.ofNullable(System.getenv("SENDGRID_API_KEY"))
                .map(SendGrid::new)
                .orElse(null);
        String fromEmail = System.getenv("FROM_EMAIL");
        EmailSender emailSender = new EmailSender(client, fromEmail);
        LHTaskWorker worker = new LHTaskWorker(emailSender, SEND_EMAIL_TASK, config);

        switch (args[0]) {
            case COMMAND_REGISTER -> {
                if (worker.doesTaskDefExist()) {
                    log.debug("Task {} already exists, skipping creation", worker.getTaskDefName());
                } else {
                    log.debug("Task {} does not exist, registering it", worker.getTaskDefName());
                    worker.registerTaskDef();
                }
            }
            case COMMAND_RUN_WORKER -> {
                worker.start();
                Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
            }
        }
    }
}
