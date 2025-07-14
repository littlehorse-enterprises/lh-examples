package io.littlehorse.services;

import com.google.protobuf.ByteString;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.VariableMatch;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.workflows.QuickstartWorkflow;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class WorkflowsService {
    private final LittleHorseBlockingStub blockingStub;

    public WorkflowsService(LittleHorseBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public WfRunIdList search(String status, String email, ByteString bookmark) {
        SearchWfRunRequest.Builder request = SearchWfRunRequest.newBuilder()
                .setWfSpecName(QuickstartWorkflow.QUICKSTART_WORKFLOW)
                .setLimit(10);

        if (Objects.nonNull(bookmark)) {
            request.setBookmark(bookmark);
        }

        if (Objects.nonNull(status)) {
            request.addVariableFilters(VariableMatch.newBuilder()
                            .setVarName(QuickstartWorkflow.APPROVAL_STATUS)
                            .setValue(VariableValue.newBuilder().setStr(status)))
                    .build();
        }
        if (Objects.nonNull(email)) {
            request.addVariableFilters(VariableMatch.newBuilder()
                            .setVarName(QuickstartWorkflow.EMAIL)
                            .setValue(VariableValue.newBuilder().setStr(email)))
                    .build();
        }

        return blockingStub.searchWfRun(request.build());
    }
}
