package com.budgettracker.repository;

import com.budgettracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "select count(*) from app_users where email = :email", nativeQuery = true)
    long countByEmailIgnoringTenant(@Param("email") String email);

    List<User> findByTenantId(UUID tenantId);
}
