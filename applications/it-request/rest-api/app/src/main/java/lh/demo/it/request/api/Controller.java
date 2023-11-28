package lh.demo.it.request.api;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("it-requests")
public class Controller {
    private final LHPublicApiGrpc.LHPublicApiBlockingStub client;

    public Controller(LHPublicApiGrpc.LHPublicApiBlockingStub client) {
        this.client = client;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public IdResponse createITRequest(@RequestBody @Valid CreateITRequest request) {
        String wfRunId = UUID.randomUUID().toString().replace("-", "");

        RunWfRequest runWf = RunWfRequest.newBuilder()
                .setId(wfRunId)
                .setWfSpecName("it-request")
                .putVariables("requester-email", LHLibUtil.objToVarVal(request.requesterEmail()))
                .putVariables("item-description", LHLibUtil.objToVarVal(request.itemDescription()))
                .build();

        client.runWf(runWf);

        return new IdResponse(wfRunId);
    }
}
