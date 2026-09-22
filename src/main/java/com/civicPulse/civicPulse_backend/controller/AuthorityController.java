package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.ComplaintRejectRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResolveRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintVerifyRequest;
import com.civicPulse.civicPulse_backend.service.ComplaintService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authority")
public class AuthorityController {

    private final ComplaintService complaintService;

    public AuthorityController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }


    // Authority: complaint verify karo
    @PatchMapping("/complaints/{id}/verify")
    public ResponseEntity<?> verifyComplaint(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ComplaintVerifyRequest request
    ) {
        String authorityEmail = authentication.getName();

        return ResponseEntity.ok(
                complaintService.verifyComplaint(authorityEmail, id, request)
        );
    }

    // Authority: complaint reject karo
    @PatchMapping("/complaints/{id}/reject")
    public ResponseEntity<?> rejectComplaint(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ComplaintRejectRequest request
    ) {
        String authorityEmail = authentication.getName();

        return ResponseEntity.ok(
                complaintService.rejectComplaint(authorityEmail, id, request)
        );
    }

    // Authority: verified complaint pe kaam shuru karo
    @PatchMapping("/complaints/{id}/start-progress")
    public ResponseEntity<?> startProgress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String authorityEmail = authentication.getName();

        return ResponseEntity.ok(
                complaintService.startProgress(authorityEmail, id)
        );
    }

    // Authority: complaint resolve karo, proof photo ke saath
    @PatchMapping("/complaints/{id}/resolve")
    public ResponseEntity<?> resolveComplaint(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ComplaintResolveRequest request
    ) {
        String authorityEmail = authentication.getName();

        return ResponseEntity.ok(
                complaintService.resolveComplaint(authorityEmail, id, request)
        );
    }
}