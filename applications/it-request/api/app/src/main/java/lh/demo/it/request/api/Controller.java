package lh.demo.it.request.api;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.ListVariablesRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.WfRunId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping(path = "/{it-request-id}")
    public ITRequest getITRequest(@PathVariable("it-request-id") String id) {

        List<Variable> variables = client.listVariables(ListVariablesRequest.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId(id))
                        .build())
                .getResultsList();

        String status = variables.stream()
                .filter(variable -> variable.getId().getName().equals("status")
                        && variable.getId().getThreadRunNumber() == 0)
                .map(variable -> variable.getValue().getStr())
                .toList()
                .get(0);

        String requesterEmail = variables.stream()
                .filter(variable -> variable.getId().getName().equals("requester-email")
                        && variable.getId().getThreadRunNumber() == 0)
                .map(variable -> variable.getValue().getStr())
                .toList()
                .get(0);

        String itemDescription = variables.stream()
                .filter(variable -> variable.getId().getName().equals("item-description")
                        && variable.getId().getThreadRunNumber() == 0)
                .map(variable -> variable.getValue().getStr())
                .toList()
                .get(0);

        return new ITRequest(id, Status.valueOf(status), requesterEmail, itemDescription);
    }

    @GetMapping
    public Paginated<ITRequest> getITRequests(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String requesterEmail,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(defaultValue = "1") @Min(1) Integer page) {
        return null;
    }

    @PostMapping(path = "/{it-request-id}/complete")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void completeITRequest(@PathVariable("it-request-id") String id, @RequestBody CompleteRequest request)
            throws LHSerdeError {

        UserTaskRunId userTaskRunId = client.listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId(id))
                        .build())
                .getResultsList()
                .get(0)
                .getId();

        CompleteUserTaskRunRequest result = CompleteUserTaskRunRequest.newBuilder()
                .setUserId(request.userId())
                .putResults("isApproved", LHLibUtil.objToVarVal(request.isApproved()))
                .setUserTaskRunId(userTaskRunId)
                .build();

        client.completeUserTaskRun(result);
    }
}