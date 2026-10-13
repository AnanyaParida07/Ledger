package com.budgettracker.config;

import com.budgettracker.entity.User;
import com.budgettracker.repository.UserRepository;
import com.budgettracker.service.JwtService;
import com.budgettracker.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Runs once per request, ahead of everything else. Three jobs:
 *
 *  1. Validate the JWT and populate Spring's SecurityContext (normal auth).
 *  2. Stash the token's tenantId in TenantContext (ThreadLocal).
 *  3. Enable the Hibernate "tenantFilter" on the current Session, so every
 *     query issued for the REST of this request is automatically scoped to
 *     that tenant - controllers and repositories never mention tenant_id.
 *
 * Step 3 relies on Spring Boot's open-in-view (default true) keeping the
 * same persistence context/Session alive for the whole request thread.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // @PersistenceContext must be field/setter injection, not a constructor
    // param - Spring's PersistenceAnnotationBeanPostProcessor injects this
    // shared, thread-bound EntityManager proxy AFTER construction.
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (jwtService.isTokenValid(token) && "access".equals(jwtService.extractTokenType(token))) {
                    UUID userId = jwtService.extractUserId(token);
                    UUID tenantId = jwtService.extractTenantId(token);

                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        TenantContext.setTenantId(tenantId);
                        entityManager.unwrap(Session.class)
                                .enableFilter("tenantFilter")
                                .setParameter("tenantId", tenantId);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            // Thread pools reuse threads across requests - without this, a
            // stale tenantId could leak into the next request on this thread.
            TenantContext.clear();
        }
    }
}
