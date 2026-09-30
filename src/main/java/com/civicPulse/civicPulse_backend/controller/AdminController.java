package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.*;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.entity.Department;
import com.civicPulse.civicPulse_backend.entity.SLARule;
import com.civicPulse.civicPulse_backend.service.*;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;
    private final SLARuleService slaRuleService;
    private final ComplaintService complaintService;

    private final StatsService statsService;
    public AdminController(
            AdminService adminService,
            CategoryService categoryService,
            SLARuleService slaRuleService,
            ComplaintService complaintService   ,

    StatsService statsService) {
        this.adminService = adminService;
        this.categoryService = categoryService;
        this.slaRuleService = slaRuleService;
        this.statsService=statsService;
        this.complaintService = complaintService;

    }


    // ===== Authority management =====

    @PostMapping("/authorities")
    public ResponseEntity<String> createAuthority(
            @Valid @RequestBody AuthorityCreateRequest request
    ) {
        return ResponseEntity.ok(adminService.createAuthority(request));
    }


    // ===== Category management =====



    // ===== SLA Rule management =====

    @PostMapping("/sla-rules")
    public ResponseEntity<SLARule> createSlaRule(
            @Valid @RequestBody SLARuleCreateRequest request
    ) {
        return ResponseEntity.ok(slaRuleService.createRule(request));
    }

    @PutMapping("/sla-rules/{id}")
    public ResponseEntity<SLARule> updateSlaRule(
            @PathVariable Long id,
            @Valid @RequestBody SLARuleUpdateRequest request
    ) {
        return ResponseEntity.ok(slaRuleService.updateRule(id, request));
    }

    @GetMapping("/sla-rules")
    public ResponseEntity<List<SLARule>> getAllSlaRules() {
        return ResponseEntity.ok(slaRuleService.getAllRules());
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return statsService.getStats();
    }

    // Admin: saare departments ki complaints (optional filters)
    @GetMapping("/complaints")
    public ResponseEntity<Page<ComplaintResponse>> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Department department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                complaintService.getAllComplaintsForAdmin(
                        status,
                        department,
                        pageable
                )
        );
    }

    @GetMapping("/authorities/workload")
    public ResponseEntity<List<AuthorityWorkloadResponse>> getAuthorityWorkload() {
        return ResponseEntity.ok(statsService.getAuthorityWorkload());
    }
    EmailService emailService;
    @GetMapping("/api/test-email")
    public String testEmail() {
        emailService.sendEmail(
                "your-email@gmail.com",
                "CivicPulse Email Test",
                "This is a test email from CivicPulse backend."
        );
        return "Email sent attempt";
    }
    @GetMapping("/authorities")
    public ResponseEntity<List<AuthorityResponse>> getAuthorities(
            @RequestParam Department department,
            @RequestParam(required = false) String ward) {

        return ResponseEntity.ok(adminService.getAuthoritiesByDepartmentAndWard(department, ward));
    }
    @PatchMapping("/complaints/{id}/assign")
    public ResponseEntity<ComplaintAssignResponse> assignComplaint(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ComplaintAssignRequest request) {

        ComplaintAssignResponse response = complaintService.assignComplaintToAuthority(
                authentication.getName(), id, request);

        return ResponseEntity.ok(response);
    }
}