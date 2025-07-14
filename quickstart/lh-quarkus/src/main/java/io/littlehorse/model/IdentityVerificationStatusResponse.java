package io.littlehorse.model;

import io.littlehorse.sdk.common.proto.Variable;

public class IdentityVerificationStatusResponse {
    private final String fullName;
    private final String email;
    private final String status;

    public IdentityVerificationStatusResponse(String fullName, String email, String status) {
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    public static IdentityVerificationStatusResponse fromProto(Variable fullName, Variable email, Variable status) {
        return new IdentityVerificationStatusResponse(
                fullName.getValue().getStr(),
                email.getValue().getStr(),
                status.getValue().getStr());
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getStatus() {
        return this.status;
    }
}
