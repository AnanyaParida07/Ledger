package com.budgettracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteUserRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email
) {
}
