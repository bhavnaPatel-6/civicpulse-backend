package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.Department;
import jakarta.validation.constraints.NotBlank;

public class AuthorityUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String phoneNumber;

    private Department department;

    private String ward;

    // Constructors
    public AuthorityUpdateRequest() {}

    public AuthorityUpdateRequest(String name, String phoneNumber, Department department, String ward) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.ward = ward;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
}