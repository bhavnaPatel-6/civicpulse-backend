package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.Department;
import com.civicPulse.civicPulse_backend.entity.Role;
import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String city;
    private String ward;
    private Role role;
    private Integer reputationPoints;
    private LocalDateTime createdAt;
    private Department department;
    public UserResponse(
            Long id,
            String name,
            String email,
            String phoneNumber,
            String city,
            String ward,
            Role role,
            Department department,
            Integer reputationPoints,
            LocalDateTime createdAt) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.ward = ward;
        this.role = role;
        this.department = department;
        this.reputationPoints = reputationPoints;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public String getWard() {
        return ward;
    }

    public Role getRole() {
        return role;
    }

    public Integer getReputationPoints() {
        return reputationPoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}