package io.littlehorse.document.processing;

import static io.littlehorse.document.processing.LHConstants.TASK_DETERMINE_APPROVAL_ROUTE;
import static io.littlehorse.document.processing.LHConstants.TASK_EXTRACT_DOCUMENT_INFO;
import static io.littlehorse.document.processing.LHConstants.TASK_NOTIFY_SUBMITTER;
import static io.littlehorse.document.processing.LHConstants.TASK_ROUTE_TO_DEPARTMENT;
import static io.littlehorse.document.processing.LHConstants.TASK_VALIDATE_DOCUMENT;

import io.littlehorse.document.processing.agent.DirectAIAgent;
import io.littlehorse.document.processing.tasks.DetermineApprovalRouteTask;
import io.littlehorse.document.processing.tasks.ExtractDocumentInfoTask;
import io.littlehorse.document.processing.tasks.NotifySubmitterTask;
import io.littlehorse.document.processing.tasks.RouteToDeprtmentTask;
import io.littlehorse.document.processing.tasks.ValidateDocumentTask;
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

    private static LHTaskWorker extractWorker =
            new LHTaskWorker(new ExtractDocumentInfoTask(), TASK_EXTRACT_DOCUMENT_INFO, config);

    private static LHTaskWorker validateWorker =
            new LHTaskWorker(new ValidateDocumentTask(), TASK_VALIDATE_DOCUMENT, config);

    private static LHTaskWorker routeWorker =
            new LHTaskWorker(new DetermineApprovalRouteTask(), TASK_DETERMINE_APPROVAL_ROUTE, config);

    private static LHTaskWorker departmentWorker =
            new LHTaskWorker(new RouteToDeprtmentTask(), TASK_ROUTE_TO_DEPARTMENT, config);

    private static LHTaskWorker notifyWorker =
            new LHTaskWorker(new NotifySubmitterTask(), TASK_NOTIFY_SUBMITTER, config);

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0].toLowerCase() : "all";

        switch (mode) {
            case "direct":
                // Demo 1: Direct AI Agent (Without LittleHorse)
                System.out.println("\n\n=========================================");
                System.out.println("DEMO 1: DIRECT AI AGENT (WITHOUT LITTLEHORSE)");
                System.out.println("=========================================");
                demoAgentWithoutLittleHorse();
                break;

            case "lh":
                // Demo 2: LittleHorse Orchestrated Workflow
                System.out.println("\n\n=========================================");
                System.out.println("DEMO 2: AI AGENT (WITH LITTLEHORSE)");
                System.out.println("=========================================");
                registerMetadata();
                demoAgentWithLittleHorse();
                startTaskWorkers();
                break;

            default:
                // Run both demos
                // Demo 1: Direct AI Agent (Without LittleHorse)
                System.out.println("\n\n=========================================");
                System.out.println("DEMO 1: DIRECT AI AGENT (WITHOUT LITTLEHORSE)");
                System.out.println("=========================================");
                demoAgentWithoutLittleHorse();

                // Demo 2: LittleHorse Orchestrated Workflow
                System.out.println("\n\n=========================================");
                System.out.println("DEMO 2: AI AGENT (WITH LITTLEHORSE)");
                System.out.println("=========================================");
                registerMetadata();
                demoAgentWithLittleHorse();
                startTaskWorkers();
                break;
        }
    }

    private static void registerMetadata() throws Exception {
        System.out.println("Registering workflow and task definitions...");

        // Register TaskDefs
        extractWorker.registerTaskDef();
        validateWorker.registerTaskDef();
        routeWorker.registerTaskDef();
        departmentWorker.registerTaskDef();
        notifyWorker.registerTaskDef();

        // Register External Event Def for document approval
        client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                .setName(LHConstants.EVENT_DOCUMENT_APPROVAL)
                .build());

        // Register workflow
        DocumentProcessingWorkflow workflow = new DocumentProcessingWorkflow();
        workflow.getWorkflow().registerWfSpec(client);

        System.out.println("Metadata registration complete!");
    }

    private static void demoAgentWithoutLittleHorse() {
        DirectAIAgent agent = new DirectAIAgent();

        // Process 3 documents to demonstrate failures
        for (int i = 0; i < 3; i++) {
            String documentId = "DOC-" + (1000 + i);
            String documentType = i % 2 == 0 ? "INVOICE" : "CONTRACT";
            String submitterId = "USER-" + (100 + i);

            System.out.println("\n--- Processing Document " + (i + 1) + " ---");
            try {
                agent.processDocument(documentId, documentType, submitterId);
            } catch (Exception e) {
                System.err.println("Document processing pipeline failed completely: " + e.getMessage());
            }
        }
    }

    private static void demoAgentWithLittleHorse() throws Exception {
        // Process 3 documents with LittleHorse workflow
        for (int i = 0; i < 3; i++) {
            String documentId = "DOC-LH-" + (1000 + i);
            String documentType = i % 2 == 0 ? "INVOICE" : "CONTRACT";
            String submitterId = "USER-" + (100 + i);

            System.out.println("\n--- Processing Document " + (i + 1) + " with LittleHorse");

            RunWfRequest.Builder reqBuilder = RunWfRequest.newBuilder().setWfSpecName(LHConstants.WORKFLOW_NAME);

            // Add required variables
            reqBuilder.putVariables("document-id", LHLibUtil.objToVarVal(documentId));
            reqBuilder.putVariables("document-type", LHLibUtil.objToVarVal(documentType));
            reqBuilder.putVariables("submitter-id", LHLibUtil.objToVarVal(submitterId));

            // Run the workflow
            WfRun wfRun = client.runWf(reqBuilder.build());
            System.out.println("Started workflow with ID: " + wfRun.getId().getId());
        }
    }

    private static void startTaskWorkers() throws Exception {
        System.out.println("\n\nStarting task workers...");

        // Close the worker upon shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(extractWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(validateWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(routeWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(departmentWorker::close));
        Runtime.getRuntime().addShutdownHook(new Thread(notifyWorker::close));

        extractWorker.start();
        validateWorker.start();
        routeWorker.start();
        departmentWorker.start();
        notifyWorker.start();
    }
}
