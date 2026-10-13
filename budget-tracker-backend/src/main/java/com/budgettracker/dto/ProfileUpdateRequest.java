package com.budgettracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(@NotBlank String fullName, @Email @NotBlank String email) {}
