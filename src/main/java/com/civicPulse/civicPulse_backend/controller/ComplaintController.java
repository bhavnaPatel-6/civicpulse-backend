package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.ComplaintCreateRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResponse;
import com.civicPulse.civicPulse_backend.entity.Department;
import com.civicPulse.civicPulse_backend.service.ComplaintService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
// Existing ComplaintController.java mein, getComplaintsByDepartment() ke neeche add karo:

    // Citizen: resolved complaint ko confirm/close karo
    @PatchMapping("/{id}/close")
    public ResponseEntity<ComplaintResponse> closeComplaint(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String citizenEmail = authentication.getName();

        return ResponseEntity.ok(
                complaintService.closeComplaint(citizenEmail, id)
        );
    }
    // Citizen: apni saari complaints (paginated)
    @GetMapping("/mine")
    public ResponseEntity<Page<ComplaintResponse>> getMyComplaints(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String citizenEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                complaintService.getMyComplaints(citizenEmail, pageable)
        );
    }

    // Authority: apne department ki saari complaints (paginated)
    @GetMapping("/department/{department}")
    public ResponseEntity<Page<ComplaintResponse>> getComplaintsByDepartment(
            @PathVariable Department department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                complaintService.getComplaintsByDepartment(department, pageable)
        );
    }
}