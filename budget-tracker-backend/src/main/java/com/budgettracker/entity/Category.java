package com.budgettracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseTenantEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean archived = false;

    public Category(String name) {
        this.name = name;
    }
}
