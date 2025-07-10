package io.littlehorse.wealth.management;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

public class RESTController {

    private Javalin server;
    private LittleHorseBlockingStub client;

    public RESTController(LHConfig config) {
        this.client = config.getBlockingStub();
        this.server = Javalin.create()
                .post("/iniate-response", this::initiateResponse)
                .post("/confirm-meeting/{incident-id}", this::confirmMeeting);
    }

    public void initiateResponse(Context ctx) {
        ResponseInitiationRequest request = ctx.bodyAsClass(ResponseInitiationRequest.class);
        WfRun out = client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName(PortfolioWorkflow.WF_NAME)
                .putVariables("portfolio-id", LHLibUtil.objToVarVal(request.portfolioId))
                .build());

        ctx.status(200);
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.result(LHLibUtil.protoToJson(out));
    }

    public void confirmMeeting(Context ctx) {
        System.out.println("hola");
        MeetingConfirmationInfo payload = ctx.bodyAsClass(MeetingConfirmationInfo.class);
        System.out.println(payload.zoomLink);
        WfRunId wfRunId = WfRunId.newBuilder().setId(ctx.pathParam("incident-id")).build();

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setWfRunId(wfRunId)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(PortfolioWorkflow.MEETING_SCHEDULED))
                .setContent(LHLibUtil.objToVarVal(payload))
                .build());

        ctx.status(200);
        ctx.contentType(ContentType.APPLICATION_JSON);
        ctx.result("{\"status\": \"ok\"}");
    }

    public void start() {
        System.out.println("Starting javalin server");
        server.start(5000);
    }
}

class ResponseInitiationRequest {
    public String portfolioId;
}
