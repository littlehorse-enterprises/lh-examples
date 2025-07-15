package io.littlehorse.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ValidateIdentityRequest {
    @NotBlank
    @Email
    private String email;

    private boolean isValid;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsValid() {
        return this.isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = true;
    }
}
