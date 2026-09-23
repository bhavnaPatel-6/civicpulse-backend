package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.SLARuleCreateRequest;
import com.civicPulse.civicPulse_backend.dto.SLARuleUpdateRequest;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.SLARule;
import com.civicPulse.civicPulse_backend.repository.SLARuleRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SLARuleService {

    private final SLARuleRepository slaRuleRepository;
    private final CategoryService categoryService;

    public SLARuleService(SLARuleRepository slaRuleRepository, CategoryService categoryService) {
        this.slaRuleRepository = slaRuleRepository;
        this.categoryService = categoryService;
    }

    // Admin: naya SLA rule banao
    public SLARule createRule(SLARuleCreateRequest request) {

        Category category = categoryService.getCategoryById(request.getCategoryId());

        boolean exists = slaRuleRepository
                .findByCategoryIdAndPriority(category.getId(), request.getPriority())
                .isPresent();

        if (exists) {
            throw new RuntimeException("SLA rule already exists for this category and priority");
        }

        SLARule rule = new SLARule(category, request.getPriority(), request.getDurationHours());
        return slaRuleRepository.save(rule);
    }

    // Admin: existing rule ki duration update karo
    public SLARule updateRule(Long ruleId, SLARuleUpdateRequest request) {

        SLARule rule = slaRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("SLA rule not found"));

        rule.setDurationHours(request.getDurationHours());
        return slaRuleRepository.save(rule);
    }

    // Sabke liye: saari rules dekho
    public List<SLARule> getAllRules() {
        return slaRuleRepository.findAll();
    }
}