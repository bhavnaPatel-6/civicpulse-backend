package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.ComplaintCreateRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResponse;
import com.civicPulse.civicPulse_backend.service.ComplaintService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // Citizen: naya complaint report karo
    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            Authentication authentication,
            @Valid @RequestBody ComplaintCreateRequest request
    ) {
        String citizenEmail = authentication.getName();

        return ResponseEntity.ok(
                complaintService.createComplaint(citizenEmail, request)
        );
    }

    // Koi bhi logged-in user: ek specific complaint dekho
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                complaintService.getComplaintById(id)
        );
    }
}