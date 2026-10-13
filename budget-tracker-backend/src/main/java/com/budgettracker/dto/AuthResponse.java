package com.budgettracker.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID tenantId,
        String role,
        boolean mustChangePassword
) {
}
