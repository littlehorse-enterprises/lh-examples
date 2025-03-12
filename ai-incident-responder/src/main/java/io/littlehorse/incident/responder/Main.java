package io.littlehorse.incident.responder;

import static io.littlehorse.incident.responder.LHConstants.EVENT_ENGINEER_RESPONSE;
import static io.littlehorse.incident.responder.LHConstants.TASK_ATTEMPT_REMEDIATION;
import static io.littlehorse.incident.responder.LHConstants.TASK_DIAGNOSE_INCIDENT;
import static io.littlehorse.incident.responder.LHConstants.TASK_ESCALATE_TO_ENGINEER;
import static io.littlehorse.incident.responder.LHConstants.TASK_NOTIFY_STATUS;
import static io.littlehorse.incident.responder.LHConstants.TASK_SEND_SLACK_ALERT;
import static io.littlehorse.incident.responder.LHConstants.TASK_VALIDATE_INCIDENT;
import static io.littlehorse.incident.responder.LHConstants.WORKFLOW_NAME;

import java.util.Random;
import java.util.UUID;

import io.littlehorse.incident.responder.agent.DirectAIAgent;
import io.littlehorse.incident.responder.tasks.AttemptRemediationTask;
import io.littlehorse.incident.responder.tasks.DiagnoseIncidentTask;
import io.littlehorse.incident.responder.tasks.EscalateToEngineerTask;
import io.littlehorse.incident.responder.tasks.NotifyStatusTask;
import io.littlehorse.incident.responder.tasks.SendSlackAlertTask;
import io.littlehorse.incident.responder.tasks.ValidateIncidentTask;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class Main {
    private static LHConfig config = new LHConfig();
    private static LittleHorseBlockingStub client = config.getBlockingStub();

    private static LHTaskWorker diagnoseWorker = new LHTaskWorker(new DiagnoseIncidentTask(), TASK_DIAGNOSE_INCIDENT,
            config);

    private static LHTaskWorker validateWorker = new LHTaskWorker(new ValidateIncidentTask(), TASK_VALIDATE_INCIDENT,
            config);

    private static LHTaskWorker remediationWorker = new LHTaskWorker(new AttemptRemediationTask(),
            TASK_ATTEMPT_REMEDIATION, config);

    private static LHTaskWorker escalateWorker = new LHTaskWorker(new EscalateToEngineerTask(),
            TASK_ESCALATE_TO_ENGINEER, config);

    private static LHTaskWorker notifyWorker = new LHTaskWorker(new NotifyStatusTask(), TASK_NOTIFY_STATUS, config);

    private static LHTaskWorker slackWorker = new LHTaskWorker(new SendSlackAlertTask(), TASK_SEND_SLACK_ALERT, config);

    private static Random random = new Random();

    public static void main(String[] args) throws Exception {
        // Determine which demo to run
        String demoType = "both"; // Default to running both demos

        if (args.length > 0) {
            demoType = args[0].toLowerCase();
        }

        System.out.println("\n======= AI INCIDENT RESPONDER DEMO =======\n");

        if (demoType.equals("direct") || demoType.equals("both")) {
            System.out.println("\n===== DEMO 1: Direct AI Agent (Without LittleHorse) =====\n");
            runDirectAIDemo();
        }

        if (demoType.equals("lh") || demoType.equals("both")) {
            System.out.println("\n===== DEMO 2: LittleHorse Orchestrated Workflow =====\n");
            registerMetadata();
            startWorkers();
            runLittleHorseDemo();
        }
    }

    private static void registerMetadata() {
        System.out.println("Registering workflow and task definitions...");

        // Register all TaskDefs first
        diagnoseWorker.registerTaskDef();
        validateWorker.registerTaskDef();
        remediationWorker.registerTaskDef();
        escalateWorker.registerTaskDef();
        notifyWorker.registerTaskDef();
        slackWorker.registerTaskDef();

        // Register external event definition
        PutExternalEventDefRequest eventDefRequest = PutExternalEventDefRequest.newBuilder()
                .setName(EVENT_ENGINEER_RESPONSE)
                .build();
        client.putExternalEventDef(eventDefRequest);

        // Register workflow
        new IncidentResponseWorkflow().getWorkflow().registerWfSpec(client);

        System.out.println("Metadata registration complete.");
    }

    private static void startWorkers() {
        System.out.println("Starting task workers...");

        // Register shutdown hooks to ensure graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(diagnoseWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(validateWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(remediationWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(escalateWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notifyWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(slackWorker::close));

        // Start all the workers
        diagnoseWorker.start();
        validateWorker.start();
        remediationWorker.start();
        escalateWorker.start();
        notifyWorker.start();
        slackWorker.start();
    }

    private static void runDirectAIDemo() {
        // Create a direct AI agent
        DirectAIAgent agent = new DirectAIAgent();

        // Create a series of random incidents
        for (int i = 0; i < 3; i++) {
            String alertId = "DIRECT-" + UUID.randomUUID().toString().substring(0, 8);
            String systemName = getRandomSystem();
            String alertType = getRandomAlertType();
            String severity = getRandomSeverity();

            System.out.println("\n----- Direct AI Demo - Incident #" + (i + 1) + " -----");
            System.out.println("Alert ID: " + alertId);
            System.out.println("System: " + systemName);
            System.out.println("Alert Type: " + alertType);
            System.out.println("Severity: " + severity);
            System.out.println("Processing...\n");

            try {
                agent.respondToIncident(alertId, systemName, alertType, severity);
            } catch (Exception e) {
                System.err.println("❌ Direct AI agent failed catastrophically: " + e.getMessage());
                System.err.println("❌ No retry mechanism available - incident processing abandoned!");
            }

            // Wait between incidents
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void runLittleHorseDemo() {
        // Run a series of incidents with LittleHorse
        for (int i = 0; i < 3; i++) {
            String alertId = "LH-" + UUID.randomUUID().toString().substring(0, 8);
            String systemName = getRandomSystem();
            String alertType = getRandomAlertType();
            String severity = getRandomSeverity();

            System.out.println("\n----- LittleHorse Demo - Incident #" + (i + 1) + " -----");
            System.out.println("Alert ID: " + alertId);
            System.out.println("System: " + systemName);
            System.out.println("Alert Type: " + alertType);
            System.out.println("Severity: " + severity);
            System.out.println("Starting workflow...\n");

            RunWfRequest.Builder requestBuilder = RunWfRequest.newBuilder()
                    .setWfSpecName(WORKFLOW_NAME)
                    .putVariables("alert-id", LHLibUtil.objToVarVal(alertId))
                    .putVariables("system-name", LHLibUtil.objToVarVal(systemName))
                    .putVariables("alert-type", LHLibUtil.objToVarVal(alertType))
                    .putVariables("severity", LHLibUtil.objToVarVal(severity));

            RunWfRequest request = requestBuilder.build();
            WfRun response = client.runWf(request);

            System.out.println("Workflow started with ID: " + response.getId().getId());
        }
    }

    private static String getRandomSystem() {
        String[] systems = {
                "payment-api",
                "user-database",
                "authentication-service",
                "product-catalog",
                "order-processing",
                "recommendation-engine"
        };
        return systems[random.nextInt(systems.length)];
    }

    private static String getRandomAlertType() {
        String[] alertTypes = { "CPU_SPIKE", "MEMORY_LEAK", "API_LATENCY", "DISK_FULL", "CONNECTION_POOL_SATURATION" };
        return alertTypes[random.nextInt(3)]; // Use only the first 3 for better demo scenarios
    }

    private static String getRandomSeverity() {
        String[] severities = { "LOW", "MEDIUM", "HIGH", "CRITICAL" };
        return severities[random.nextInt(severities.length)];
    }
}
