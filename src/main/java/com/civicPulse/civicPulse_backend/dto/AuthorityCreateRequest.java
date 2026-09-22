package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.Department;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthorityCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(
            min = 2,
            max = 50,
            message = "Name must be between 2 and 50 characters"
    )
    private String name;


    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;


    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Enter a valid 10-digit phone number"
    )
    private String phoneNumber;


    @NotBlank(message = "City is required")
    private String city;


    @NotBlank(message = "Ward is required")
    private String ward;


    @NotNull(message = "Department is required")
    private Department department;


    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            message = "Password must be at least 8 characters"
    )
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!]).{8,}$",
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    private String password;


    // Getters

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

    public Department getDepartment() {
        return department;
    }

    public String getPassword() {
        return password;
    }


    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}