package lh.demo.it.request.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteRequest(String observations, @NotBlank String userId, @NotNull Boolean isApproved) {
}
