package io.littlehorse.services;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.workflows.QuickstartWorkflow;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IdentityService {

    private final LittleHorseBlockingStub blockingStub;

    public IdentityService(LittleHorseBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public String startIdentityVerification(String fullName, String email, int ssn) {
        RunWfRequest request = RunWfRequest.newBuilder()
                .setWfSpecName(QuickstartWorkflow.QUICKSTART_WORKFLOW)
                .putVariables(QuickstartWorkflow.FULL_NAME, LHLibUtil.objToVarVal(fullName))
                .putVariables(QuickstartWorkflow.EMAIL, LHLibUtil.objToVarVal(email))
                .putVariables(QuickstartWorkflow.SSN, LHLibUtil.objToVarVal(ssn))
                .build();

        return blockingStub.runWf(request).getId().getId();
    }
}
