package io.littlehorse.model;

import io.littlehorse.sdk.common.proto.WfRunId;

public class VerifyIdentityResponse {
    private final String wfRunId;

    public VerifyIdentityResponse(String wfRunId) {
        this.wfRunId = wfRunId;
    }

    public static VerifyIdentityResponse fromWfRunIdProto(WfRunId wfRunId) {
        return new VerifyIdentityResponse(wfRunId.getId());
    }

    public String getWfRunId() {
        return this.wfRunId;
    }
}
