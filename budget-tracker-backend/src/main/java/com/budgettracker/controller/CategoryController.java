package com.budgettracker.controller;

import com.budgettracker.entity.Category;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.budgettracker.dto.CategoryUpdateRequest;

/**
 * Tenant isolation is enforced at the repository boundary. Do not use
 * findAll() for tenant-scoped entities in request handlers.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> list() {
        return categoryRepository.findByTenantId(currentTenantId()).stream().filter(category -> !category.isArchived()).toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Category update(@PathVariable UUID id, @RequestBody CategoryUpdateRequest request) {
        Category category = categoryRepository.findByIdAndTenantId(id, currentTenantId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setName(request.name());
        return categoryRepository.save(category);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void archive(@PathVariable UUID id) {
        Category category = categoryRepository.findByIdAndTenantId(id, currentTenantId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setArchived(true);
        categoryRepository.save(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Category create(@RequestBody Map<String, String> body) {
        Category category = new Category(body.get("name"));
        category.setTenantId(currentTenantId());
        return categoryRepository.save(category);
    }

    private UUID currentTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing tenant context");
        }
        return tenantId;
    }
}
