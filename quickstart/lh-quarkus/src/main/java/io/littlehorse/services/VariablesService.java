package io.littlehorse.services;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.sdk.common.proto.WfRunId;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VariablesService {
    private final LittleHorseBlockingStub blockingStub;

    public VariablesService(LittleHorseBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public Variable get(String variableName, String wfRunId) {
        VariableId varId = VariableId.newBuilder()
                .setName(variableName)
                .setWfRunId(WfRunId.newBuilder().setId(wfRunId))
                .setThreadRunNumber(0)
                .build();
        return blockingStub.getVariable(varId);
    }
}
