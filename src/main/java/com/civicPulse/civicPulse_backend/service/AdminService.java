package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.AuthorityCreateRequest;
import com.civicPulse.civicPulse_backend.entity.Role;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}