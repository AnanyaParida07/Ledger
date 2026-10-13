package com.budgettracker.tenant;

import java.util.UUID;

/**
 * Holds the current request's tenant id in a ThreadLocal.
 *
 * Set by JwtAuthenticationFilter after validating the JWT, and MUST be
 * cleared at the end of every request. Thread-pooled servers reuse threads
 * across requests - without clearing, tenant A's id could leak into a
 * request from tenant B that happens to land on the same thread.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
