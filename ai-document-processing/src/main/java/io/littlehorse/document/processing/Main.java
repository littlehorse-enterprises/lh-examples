package io.littlehorse.document.processing;

import java.util.UUID;

import io.littlehorse.document.processing.agent.DirectAIAgent;
import io.littlehorse.document.processing.tasks.ExtractDocumentInfoTask;
import io.littlehorse.document.processing.tasks.NotifySubmitterTask;
import io.littlehorse.document.processing.tasks.ValidateDocumentTask;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.worker.LHTaskWorker;

public class Main {
    public static void main(String[] args) throws Exception {
        registerMetadata();

        // Demo 1: Direct AI Agent (Without LittleHorse)
        System.out.println("\n\n=========================================");
        System.out.println("DEMO 1: DIRECT AI AGENT (WITHOUT LITTLEHORSE)");
        System.out.println("=========================================");
        demoDirectAgent();

        // Demo 2: Simulated LittleHorse Orchestrated Workflow
        System.out.println("\n\n=========================================");
        System.out.println("DEMO 2: AI AGENT (WITH LITTLEHORSE)");
        System.out.println("=========================================");
        demoLittleHorseWorkflow();

        // Start workers to execute tasks
        startTaskWorkers();

    }

    private static void registerMetadata() {
        System.out.println("Registering metadata with LittleHorse...");
        LHConfig config = new LHConfig();

        // First, register the ExternalEventDef
        System.out.println("Registering External Event Definition...");
        config.getBlockingStub()
                .putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                        .setName("document-processing-result")
                        .build());

        // Next, register all TaskDefs
        System.out.println("Registering Task Definitions...");

        // Create individual task implementation instances
        ExtractDocumentInfoTask extractTask = new ExtractDocumentInfoTask();
        ValidateDocumentTask validateTask = new ValidateDocumentTask();
        NotifySubmitterTask notifyTask = new NotifySubmitterTask();

        // Register each task definition with its own worker
        LHTaskWorker extractDataWorker = new LHTaskWorker(extractTask, "extract-document-data", config);
        extractDataWorker.registerTaskDef();

        LHTaskWorker validateDataWorker = new LHTaskWorker(validateTask, "validate-document-data", config);
        validateDataWorker.registerTaskDef();

        // Add the missing task
        LHTaskWorker validateDocWorker = new LHTaskWorker(validateTask, "validate-document", config);
        validateDocWorker.registerTaskDef();

        LHTaskWorker notifySuccessWorker = new LHTaskWorker(notifyTask, "notify-processing-success", config);
        notifySuccessWorker.registerTaskDef();

        LHTaskWorker notifyFailureWorker = new LHTaskWorker(notifyTask, "notify-processing-failure", config);
        notifyFailureWorker.registerTaskDef();

        // Close workers (since we're just registering definitions)
        extractDataWorker.close();
        validateDataWorker.close();
        validateDocWorker.close();
        notifySuccessWorker.close();
        notifyFailureWorker.close();

        // Finally, register the WfSpec
        System.out.println("Registering Workflow Specification...");
        DocumentProcessingWorkflow workflow = new DocumentProcessingWorkflow();
        workflow.getWorkflow().registerWfSpec(config.getBlockingStub());

        System.out.println("Successfully registered all metadata!");
    }

    private static void startTaskWorkers() {
        System.out.println("Starting document processing task workers...");
        LHConfig config = new LHConfig();

        // Create individual task implementation instances
        ExtractDocumentInfoTask extractTask = new ExtractDocumentInfoTask();
        ValidateDocumentTask validateTask = new ValidateDocumentTask();
        NotifySubmitterTask notifyTask = new NotifySubmitterTask();

        // Create workers for each task
        LHTaskWorker extractDataWorker = new LHTaskWorker(extractTask, "extract-document-data", config);
        LHTaskWorker validateDataWorker = new LHTaskWorker(validateTask, "validate-document-data", config);
        LHTaskWorker validateDocWorker = new LHTaskWorker(validateTask, "validate-document", config);
        LHTaskWorker notifySuccessWorker = new LHTaskWorker(notifyTask, "notify-processing-success", config);
        LHTaskWorker notifyFailureWorker = new LHTaskWorker(notifyTask, "notify-processing-failure", config);

        // Register shutdown hooks
        Runtime.getRuntime().addShutdownHook(new Thread(extractDataWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(validateDataWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(validateDocWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notifySuccessWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notifyFailureWorker::close));

        // Start workers
        extractDataWorker.start();
        validateDataWorker.start();
        validateDocWorker.start();
        notifySuccessWorker.start();
        notifyFailureWorker.start();

        System.out.println("Task workers started. Press CTRL+C to stop.");
    }

    private static void demoDirectAgent() {
        DirectAIAgent agent = new DirectAIAgent();

        // Process a few documents to demonstrate failures
        for (int i = 0; i < 5; i++) {
            String documentId = "DOC-" + (1000 + i);
            String documentType = i % 2 == 0 ? "INVOICE" : "CONTRACT";
            String submitterId = "USER-" + (100 + i);

            System.out.println("\n--- PROCESSING DOCUMENT " + (i + 1) + " ---");
            try {
                agent.processDocument(documentId, documentType, submitterId);
            } catch (Exception e) {
                System.err.println("Document processing pipeline failed completely");
            }
        }
    }

    private static void demoLittleHorseWorkflow() throws Exception {
        // Create a LittleHorse client
        LHConfig config = new LHConfig();
        LittleHorseBlockingStub client = config.getBlockingStub();

        // Run the workflow a few times to showcase retry and resilience
        for (int i = 0; i < 5; i++) {
            String documentId = "LH-DOC-" + (1000 + i);
            String documentType = i % 2 == 0 ? "INVOICE" : "CONTRACT";
            String submitterId = "LH-USER-" + (100 + i);
            String runId = UUID.randomUUID().toString();

            System.out.println("\n--- STARTING LITTLEHORSE WORKFLOW FOR DOCUMENT " + (i + 1) + " ---");

            // Run the actual workflow using the proper API
            WfRun result = client.runWf(RunWfRequest.newBuilder()
                    .setWfSpecName("document-processing")
                    .setId(runId)
                    .putVariables("documentId", LHLibUtil.objToVarVal(documentId))
                    .putVariables("documentType", LHLibUtil.objToVarVal(documentType))
                    .putVariables("submitterId", LHLibUtil.objToVarVal(submitterId))
                    .build());

            System.out.println("Started workflow run: " + result.getId());
            System.out.println("Check the LittleHorse Dashboard to see the workflow progress and retries");
        }
    }
}