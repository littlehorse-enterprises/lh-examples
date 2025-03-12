package lh.demo.it.request.api;

import com.google.protobuf.ByteString;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.ListVariablesRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableMatch;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin("http://localhost:3000")
public class Controller {
    private final LittleHorseBlockingStub client;

    public Controller(LittleHorseBlockingStub client) {
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
                .putVariables("item-description", LHLibUtil.objToVarVal(request.description()))
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

        String description = variables.stream()
                .filter(variable -> variable.getId().getName().equals("item-description")
                        && variable.getId().getThreadRunNumber() == 0)
                .map(variable -> variable.getValue().getStr())
                .toList()
                .get(0);

        String comments = client
                .listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId(id))
                        .build())
                .getResultsList()
                .stream()
                .findFirst()
                .map(userTaskRun -> {
                    VariableValue variable = userTaskRun.getResultsOrDefault("comments", null);
                    if (variable == null) {
                        return null;
                    }
                    return variable.getStr();
                })
                .orElse(null);

        return new ITRequest(id, Status.valueOf(status), requesterEmail, description, comments);
    }

    @GetMapping
    public Paginated<ITRequest> getITRequests(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String requesterEmail,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(required = false) String bookmark) {

        SearchWfRunRequest.Builder search =
                SearchWfRunRequest.newBuilder().setWfSpecName("it-request").setLimit(pageSize == null ? 20 : pageSize);

        // Handle request pagination
        if (bookmark != null) {
            search.setBookmark(decode(bookmark));
        }

        // Configure search
        if (status != null) {
            search.addVariableFilters(
                    VariableMatch.newBuilder().setVarName("status").setValue(LHLibUtil.objToVarVal(status.toString())));
        }
        if (requesterEmail != null) {
            search.addVariableFilters(VariableMatch.newBuilder()
                    .setVarName("requester-email")
                    .setValue(LHLibUtil.objToVarVal(requesterEmail)));
        }

        WfRunIdList searchResult = client.searchWfRun(search.build());
        List<ITRequest> matchingRequests = searchResult.getResultsList().stream()
                .map(wfRunId -> getITRequest(wfRunId.getId()))
                .toList();
        String nextBookmark =
                searchResult.hasBookmark() ? encode(searchResult.getBookmark().toByteArray()) : null;
        return new Paginated<>(matchingRequests, nextBookmark);
    }

    private String encode(byte[] bookmark) {
        return Base64.encodeBase64String(bookmark);
    }

    private ByteString decode(String bookmark) {
        return ByteString.copyFrom(Base64.decodeBase64(bookmark));
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

        CompleteUserTaskRunRequest.Builder result = CompleteUserTaskRunRequest.newBuilder()
                .setUserId(request.userId())
                .putResults("isApproved", LHLibUtil.objToVarVal(request.isApproved()))
                .setUserTaskRunId(userTaskRunId);

        if (request.comments() != null) {
            result.putResults("comments", LHLibUtil.objToVarVal(request.comments()));
        }

        client.completeUserTaskRun(result.build());
    }
}
