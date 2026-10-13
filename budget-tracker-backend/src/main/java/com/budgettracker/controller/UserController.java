package com.budgettracker.controller;

import com.budgettracker.dto.InviteUserRequest;
import com.budgettracker.dto.InviteUserResponse;
import com.budgettracker.dto.UserResponse;
import com.budgettracker.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import com.budgettracker.dto.ProfileUpdateRequest;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return userManagementService.listUsers();
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InviteUserResponse> invite(@Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.inviteUser(request));
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) { return userManagementService.currentUser(authentication); }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody ProfileUpdateRequest request, Authentication authentication) {
        return userManagementService.updateUser(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id, Authentication authentication) {
        userManagementService.deleteUser(id, authentication);
    }
}
