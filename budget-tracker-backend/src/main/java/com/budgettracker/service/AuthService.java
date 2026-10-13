package com.budgettracker.service;

import com.budgettracker.dto.AuthResponse;
import com.budgettracker.dto.ChangePasswordRequest;
import com.budgettracker.dto.LoginRequest;
import com.budgettracker.dto.RegisterRequest;
import com.budgettracker.entity.Role;
import com.budgettracker.entity.Tenant;
import com.budgettracker.entity.User;
import com.budgettracker.repository.TenantRepository;
import com.budgettracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registration creates a brand new Tenant plus its first user as ADMIN.
     * This is the "sign up your company" flow every B2B SaaS starts with.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Tenant tenant = tenantRepository.save(new Tenant(request.companyName()));

        User user = new User();
        // Set explicitly rather than relying on TenantContext/@PrePersist:
        // at signup time no JWT has been issued yet, so there's no request
        // thread with a tenant already attached.
        user.setTenantId(tenant.getId());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        return buildAuthResponse(user);
    }

    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        User authenticatedUser = (User) authentication.getPrincipal();
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Choose a password different from your current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user.getTenantId(),
                user.getRole().name(),
                user.isMustChangePassword()
        );
    }
}
