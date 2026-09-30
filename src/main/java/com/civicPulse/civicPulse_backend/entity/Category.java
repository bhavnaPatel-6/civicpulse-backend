package com.civicPulse.civicPulse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_categories_name",
                        columnNames = "name"
                )
        }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Department defaultDepartment;

    // NEW: risk severity 1 (low) .. 5 (very dangerous). Priority Engine isse use karta hai.
    // columnDefinition default isliye, taaki purani rows wali table mein column add ho sake.
    @Column(nullable = false, columnDefinition = "integer default 3")
    private Integer severity = 3;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }
        if (this.severity == null) {
            this.severity = 3;
        }
    }

    public Category() {
    }

    public Category(String name, Department defaultDepartment) {
        this.name = name;
        this.defaultDepartment = defaultDepartment;
        this.active = true;
        this.severity = 3;
    }

    // NEW
    public Category(String name, Department defaultDepartment, Integer severity) {
        this(name, defaultDepartment);
        this.severity = severity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDefaultDepartment() {
        return defaultDepartment;
    }

    public void setDefaultDepartment(Department defaultDepartment) {
        this.defaultDepartment = defaultDepartment;
    }

    // NEW
    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}