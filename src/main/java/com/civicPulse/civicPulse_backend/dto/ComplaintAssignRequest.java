package com.civicPulse.civicPulse_backend.dto;

import jakarta.validation.constraints.NotNull;

public class ComplaintAssignRequest {

    @NotNull(message = "authorityId is required")
    private Long authorityId;

    public Long getAuthorityId() { return authorityId; }
    public void setAuthorityId(Long authorityId) { this.authorityId = authorityId; }
}