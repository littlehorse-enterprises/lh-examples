package io.littlehorse.model;

import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import java.util.List;

public class ValidateIdentityResponse {
    private List<String> wfRunIds;

    public ValidateIdentityResponse(List<String> wfRunIds) {
        this.wfRunIds = wfRunIds;
    }

    public static ValidateIdentityResponse fromCorrelatedEvent(CorrelatedEvent correlatedEvent) {
        List<String> wfRunIds = correlatedEvent.getExternalEventsList().stream()
                .map(id -> id.getWfRunId().getId())
                .toList();

        return new ValidateIdentityResponse(wfRunIds);
    }

    public List<String> getWfRunIds() {
        return this.wfRunIds;
    }
}
