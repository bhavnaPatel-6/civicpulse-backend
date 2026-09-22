package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.AuthorityCreateRequest;
import com.civicPulse.civicPulse_backend.service.AdminService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;


    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    @PostMapping("/authorities")
    public ResponseEntity<String> createAuthority(
            @Valid @RequestBody AuthorityCreateRequest request
    ) {

        return ResponseEntity.ok(
                adminService.createAuthority(request)
        );
    }
}