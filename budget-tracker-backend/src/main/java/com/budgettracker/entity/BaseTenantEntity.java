package com.budgettracker.entity;

import com.budgettracker.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Every tenant-scoped entity (User, Category, Transaction, ...) extends this.
 *
 * @FilterDef/@Filter define a Hibernate filter named "tenantFilter" that, once
 * ENABLED on the current Session, appends "AND tenant_id = :tenantId" to every
 * SELECT Hibernate issues for subclasses of this class. It is inherited by
 * every entity that extends this mapped superclass - no need to repeat the
 * annotation on Category, Transaction, etc.
 *
 * Enabling happens once per request in JwtAuthenticationFilter, not here.
 * If nothing enables the filter (e.g. during login, before we know who the
 * user is), queries simply run unfiltered - that's intentional.
 *
 * @PrePersist auto-stamps new rows with the current tenant so services don't
 * need to set tenantId by hand on every entity they create.
 */
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public abstract class BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @PrePersist
    protected void assignTenantIfMissing() {
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
    }
}
