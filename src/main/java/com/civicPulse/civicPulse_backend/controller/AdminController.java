package com.civicPulse.civicPulse_backend.controller;

import com.civicPulse.civicPulse_backend.dto.*;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.SLARule;
import com.civicPulse.civicPulse_backend.service.AdminService;
import com.civicPulse.civicPulse_backend.service.CategoryService;
import com.civicPulse.civicPulse_backend.service.SLARuleService;

import com.civicPulse.civicPulse_backend.service.StatsService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;
    private final SLARuleService slaRuleService;


    private final StatsService statsService;
    public AdminController(
            AdminService adminService,
            CategoryService categoryService,
            SLARuleService slaRuleService,
    StatsService statsService) {
        this.adminService = adminService;
        this.categoryService = categoryService;
        this.slaRuleService = slaRuleService;
        this.statsService=statsService;

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
}