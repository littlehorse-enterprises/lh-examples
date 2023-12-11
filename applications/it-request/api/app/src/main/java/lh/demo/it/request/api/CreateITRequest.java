package lh.demo.it.request.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateITRequest(@Email @NotBlank String requesterEmail, @NotBlank String description) {}
