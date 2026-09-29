package com.civicPulse.civicPulse_backend.dto;

public class AuthorityWorkloadResponse {

    private Long authorityId;
    private String authorityName;
    private String department;
    private Long totalAssigned;
    private Long pendingCount;
    private Long inProgressCount;
    private Long resolvedCount;

    public AuthorityWorkloadResponse(
            Long authorityId, String authorityName, String department,
            Long totalAssigned, Long pendingCount, Long inProgressCount, Long resolvedCount) {
        this.authorityId = authorityId;
        this.authorityName = authorityName;
        this.department = department;
        this.totalAssigned = totalAssigned;
        this.pendingCount = pendingCount;
        this.inProgressCount = inProgressCount;
        this.resolvedCount = resolvedCount;
    }

    public Long getAuthorityId() { return authorityId; }
    public String getAuthorityName() { return authorityName; }
    public String getDepartment() { return department; }
    public Long getTotalAssigned() { return totalAssigned; }
    public Long getPendingCount() { return pendingCount; }
    public Long getInProgressCount() { return inProgressCount; }
    public Long getResolvedCount() { return resolvedCount; }
}