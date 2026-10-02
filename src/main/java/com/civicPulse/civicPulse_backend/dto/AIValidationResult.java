package com.civicPulse.civicPulse_backend.dto;

public record AIValidationResult(
        boolean valid,
        boolean spam,
        boolean isPublicIssue,
        boolean evidenceSufficient,
        String suggestedSeverity,
        String reason
) {}