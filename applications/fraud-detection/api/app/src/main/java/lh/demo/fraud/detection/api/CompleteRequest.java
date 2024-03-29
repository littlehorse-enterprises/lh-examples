package lh.demo.fraud.detection.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteRequest(String comments, @NotBlank String userId, @NotNull Boolean isApproved) {}
