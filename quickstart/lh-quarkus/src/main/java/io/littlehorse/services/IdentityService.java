package io.littlehorse.services;

import io.littlehorse.model.IdentityVerificationStatus;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.workflows.IdentityVerificationWorkflow;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IdentityService {
    @Inject
    private VariablesService variablesService;

    private final LittleHorseBlockingStub blockingStub;

    public IdentityService(LittleHorseBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public WfRunId startVerification(String fullName, String email, int ssn) {
        RunWfRequest request = RunWfRequest.newBuilder()
                .setWfSpecName(IdentityVerificationWorkflow.IDENTITY_VERIFICATION_WORKFLOW)
                .putVariables(IdentityVerificationWorkflow.FULL_NAME, LHLibUtil.objToVarVal(fullName))
                .putVariables(IdentityVerificationWorkflow.EMAIL, LHLibUtil.objToVarVal(email))
                .putVariables(IdentityVerificationWorkflow.SSN, LHLibUtil.objToVarVal(ssn))
                .build();

        return blockingStub.runWf(request).getId();
    }

    public CorrelatedEvent validate(String email, boolean isValid) {
        PutCorrelatedEventRequest request = PutCorrelatedEventRequest.newBuilder()
                .setKey(email)
                .setContent(VariableValue.newBuilder().setBool(isValid))
                .setExternalEventDefId(
                        ExternalEventDefId.newBuilder().setName(IdentityVerificationWorkflow.IDENTITY_VERIFIED_EVENT))
                .build();

        return blockingStub.putCorrelatedEvent(request);
    }

    public IdentityVerificationStatus getStatus(String wfRunId) {
        String fullName = variablesService.getStringVariable(IdentityVerificationWorkflow.FULL_NAME, wfRunId);
        String email = variablesService.getStringVariable(IdentityVerificationWorkflow.EMAIL, wfRunId);
        String status = variablesService.getStringVariable(IdentityVerificationWorkflow.APPROVAL_STATUS, wfRunId);

        return new IdentityVerificationStatus(fullName, email, status, wfRunId);
    }
}
