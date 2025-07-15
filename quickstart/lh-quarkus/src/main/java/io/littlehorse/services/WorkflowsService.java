package io.littlehorse.services;

import io.littlehorse.model.SearchBookmark;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.VariableMatch;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.workflows.IdentityVerificationWorkflow;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class WorkflowsService {
    private final LittleHorseBlockingStub blockingStub;

    public WorkflowsService(LittleHorseBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public WfRunIdList search(String status, String email, SearchBookmark bookmark) {
        SearchWfRunRequest.Builder request = SearchWfRunRequest.newBuilder()
                .setWfSpecName(IdentityVerificationWorkflow.IDENTITY_VERIFICATION_WORKFLOW)
                .setLimit(10);

        if (Objects.nonNull(bookmark)) {
            request.setBookmark(bookmark.toByteString());
        }

        if (Objects.nonNull(status)) {
            request.addVariableFilters(VariableMatch.newBuilder()
                            .setVarName(IdentityVerificationWorkflow.APPROVAL_STATUS)
                            .setValue(VariableValue.newBuilder().setStr(status)))
                    .build();
        }
        if (Objects.nonNull(email)) {
            request.addVariableFilters(VariableMatch.newBuilder()
                            .setVarName(IdentityVerificationWorkflow.EMAIL)
                            .setValue(VariableValue.newBuilder().setStr(email)))
                    .build();
        }

        WfRunIdList wfRunIdList = blockingStub.searchWfRun(request.build());

        return wfRunIdList;
    }
}
