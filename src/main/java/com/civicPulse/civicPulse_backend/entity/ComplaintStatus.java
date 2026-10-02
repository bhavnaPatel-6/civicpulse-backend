package com.civicPulse.civicPulse_backend.entity;

public enum ComplaintStatus {
    PENDING_VERIFICATION,
    AUTO_VALIDATED,       // AI ne visual + text se pass kiya
    NEEDS_EVIDENCE,       // Jhooth/indoor/insufficient proof
    VERIFIED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REJECTED
}