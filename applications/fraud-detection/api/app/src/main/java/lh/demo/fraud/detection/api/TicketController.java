package lh.demo.fraud.detection.api;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tickets")
public class TicketController {

    private final LittleHorseGrpc.LittleHorseBlockingStub client;

    public TicketController(LittleHorseGrpc.LittleHorseBlockingStub client) {
        this.client = client;
    }

    @PostMapping(path = "/{transaction-id}/complete")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void completeTicket(
            @PathVariable("transaction-id") String transactionId, @RequestBody @Valid CompleteRequest request)
            throws LHSerdeError {

        UserTaskRunId userTaskRunId = client.listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId(transactionId))
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
