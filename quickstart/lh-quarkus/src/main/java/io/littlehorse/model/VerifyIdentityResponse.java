package io.littlehorse.model;

public class VerifyIdentityResponse {
    private final String wfRunId;

    public VerifyIdentityResponse(String wfRunId) {
        this.wfRunId = wfRunId;
    }

    public String getWfRunId() {
        return this.wfRunId;
    }
}
