package com.budgettracker.dto;

import java.util.UUID;

public record InviteUserResponse(
        UUID id,
        String fullName,
        String email,
        String role,
        UUID tenantId
) {
}
