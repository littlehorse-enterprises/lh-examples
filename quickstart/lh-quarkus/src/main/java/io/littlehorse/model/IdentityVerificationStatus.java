package io.littlehorse.model;

public class IdentityVerificationStatus {
    private final String fullName;
    private final String email;
    private final String status;

    public IdentityVerificationStatus(String fullName, String email, String status) {
        this.fullName = fullName;
        this.email = email;
        this.status = status;
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
