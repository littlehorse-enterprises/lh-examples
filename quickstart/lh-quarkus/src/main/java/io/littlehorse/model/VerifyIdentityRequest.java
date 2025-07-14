package io.littlehorse.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class VerifyIdentityRequest {
    @NotBlank(message = "Missing required argument: 'fullName'")
    private String fullName;

    @Email
    @NotBlank(message = "Missing required argument: 'email'")
    private String email;

    @NotNull(message = "Missing required argument: 'ssn'")
    @PositiveOrZero
    private Integer ssn; // Consider masking or encrypting this in production

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSsn() {
        return ssn;
    }

    public void setSsn(Integer ssn) {
        this.ssn = ssn;
    }
}
