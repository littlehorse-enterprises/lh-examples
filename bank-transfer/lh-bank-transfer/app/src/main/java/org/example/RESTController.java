package org.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import java.util.UUID;
import org.example.model.InitiateTransferRequest;

// TODO: GetTransfer

public class RESTController {

    private static final WorkflowEventDefId TRANSFER_FAILED =
            WorkflowEventDefId.newBuilder().setName("transfer-failed").build();
    private static final WorkflowEventDefId TRANSFER_COMPLETED =
            WorkflowEventDefId.newBuilder().setName("transfer-completed").build();

    private Javalin server;
    private LittleHorseBlockingStub client;

    public RESTController(LHConfig config) {
        this.client = config.getBlockingStub();
        this.server = Javalin.create();

        this.server.post("/transfer", this::initiateTransfer);
    }

    public void start() {
        server.start();
    }

    private void initiateTransfer(Context ctx) {
        InitiateTransferRequest request = ctx.bodyAsClass(InitiateTransferRequest.class);

        // Best practice is to always specify a WfRunId for idempotency.
        String rawWfRunId = request.idempotencyId == null ? UUID.randomUUID().toString() : request.idempotencyId;

        WfRun wfRun = client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName("initiate-transfer")
                .setId(rawWfRunId)
                .putVariables("from-account-id", LHLibUtil.objToVarVal(request.fromAccountId))
                .putVariables("to-account-id", LHLibUtil.objToVarVal(request.toAccountId))
                .putVariables("amount", LHLibUtil.objToVarVal(request.amount))
                .putVariables("currency", LHLibUtil.objToVarVal(request.currency))
                .putVariables("description", LHLibUtil.objToVarVal(request.description))
                .build());

        WfRunId wfRunId = wfRun.getId();

        // Await Workflow Event
        WorkflowEvent result = client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                .addEventDefIds(TRANSFER_COMPLETED)
                .addEventDefIds(TRANSFER_FAILED)
                .setWfRunId(wfRunId)
                .build());

        WorkflowEventDefId resultingEventId = result.getId().getWorkflowEventDefId();
        if (resultingEventId.getName().equals("transfer-failed")) {
            // Return a HTTP 400 that the transfer has failed.
            ctx.json("Transfer failed.").status(400);
        } else {
            // return 200
            ctx.json(request.description).status(HttpStatus.ACCEPTED);
        }
    }
}
