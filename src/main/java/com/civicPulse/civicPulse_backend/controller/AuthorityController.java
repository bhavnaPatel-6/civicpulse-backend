package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.ComplaintRejectRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResolveRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResponse;
import com.civicPulse.civicPulse_backend.dto.ComplaintVerifyRequest;
import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.service.ComplaintService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            @PathVariable Long id
    ) {
        String authorityEmail = authentication.getName();

        return ResponseEntity.ok(
                complaintService.verifyComplaint(authorityEmail, id)
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
    // Authority: apne department ki complaints (optional status filter)
    @GetMapping("/complaints")
    public ResponseEntity<Page<ComplaintResponse>> getDepartmentComplaints(
            Authentication authentication,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String authorityEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                complaintService.getComplaintsForAuthority(
                        authorityEmail,
                        status,
                        pageable
                )
        );
    }

    @GetMapping("/complaints/assigned-to-me")
    public ResponseEntity<Page<ComplaintResponse>> getMyAssignedComplaints(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String authorityEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                complaintService.getMyAssignedComplaints(authorityEmail, pageable)
        );
    }

}