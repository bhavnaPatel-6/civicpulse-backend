package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.AuthorityCreateRequest;
import com.civicPulse.civicPulse_backend.dto.AuthorityResponse;
import com.civicPulse.civicPulse_backend.entity.Department;
import com.civicPulse.civicPulse_backend.entity.Role;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String createAuthority(
            AuthorityCreateRequest request
    ) {

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already registered";
        }
        // Encrypt password
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // Create authority
        User authority = new User(
                request.getName(),
                request.getEmail(),
                encodedPassword,
                request.getPhoneNumber(),
                request.getCity(),
                request.getWard()
        );

        // Change role from CITIZEN to AUTHORITY
        authority.setRole(Role.AUTHORITY);

        // Set department
        authority.setDepartment(
                request.getDepartment()
        );
        // Save
        userRepository.save(authority);

        return "Authority account created successfully";


    }
    public List<AuthorityResponse> getAuthoritiesByDepartmentAndWard(Department department, String ward) {

        List<User> authorities = (ward != null && !ward.isBlank())
                ? userRepository.findByRoleAndDepartmentAndWard(Role.AUTHORITY, department, ward)
                : userRepository.findByRoleAndDepartment(Role.AUTHORITY, department);

        return authorities.stream()
                .map(a -> new AuthorityResponse(a.getId(), a.getName(), a.getEmail(), a.getWard(), a.getDepartment()))
                .toList();
    }

}