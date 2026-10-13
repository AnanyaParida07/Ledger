package com.budgettracker.service;

import com.budgettracker.dto.InviteUserRequest;
import com.budgettracker.dto.InviteUserResponse;
import com.budgettracker.dto.UserResponse;
import com.budgettracker.dto.ProfileUpdateRequest;
import com.budgettracker.entity.Role;
import com.budgettracker.entity.User;
import com.budgettracker.repository.UserRepository;
import com.budgettracker.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationEmailService invitationEmailService;

    @Transactional
    public InviteUserResponse inviteUser(InviteUserRequest request) {
        if (userRepository.countByEmailIgnoringTenant(request.email()) > 0) {
            throw new IllegalArgumentException("Email already in use");
        }

        UUID tenantId = currentTenantId();
        String temporaryPassword = generateTemporaryPassword();

        User user = new User();
        user.setTenantId(tenantId);
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setRole(Role.MEMBER);

        User saved = userRepository.save(user);
        invitationEmailService.sendInvitation(saved, temporaryPassword);
        return new InviteUserResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getTenantId()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        UUID tenantId = currentTenantId();
        return userRepository.findByTenantId(tenantId).stream()
                .filter(User::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return toResponse(userRepository.findById(user.getId()).orElseThrow());
    }

    @Transactional
    public UserResponse updateUser(UUID id, ProfileUpdateRequest request, Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        boolean admin = actor.getRole() == Role.ADMIN;
        if (!admin && !actor.getId().equals(id)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own profile");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.countByEmailIgnoringTenant(request.email()) > 0) {
            throw new IllegalArgumentException("Email already in use");
        }
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id, Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        if (actor.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can remove team members");
        }
        if (actor.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot remove your own account");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getTenantId().equals(currentTenantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing tenant context");
        }
        return tenantId;
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[18];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
