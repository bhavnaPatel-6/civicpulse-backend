package com.civicPulse.civicPulse_backend.dto;

import java.util.Map;

public class AdminStatsResponse {

    private long totalComplaints;
    private long resolved;
    private long pending;
    private long inProgress;
    private long rejected;
    private Double avgResolutionTimeHours;
    private Map<String, Long> categoryCounts;

    public AdminStatsResponse(
            long totalComplaints, long resolved, long pending, long inProgress,
            long rejected, Double avgResolutionTimeHours, Map<String, Long> categoryCounts) {
        this.totalComplaints = totalComplaints;
        this.resolved = resolved;
        this.pending = pending;
        this.inProgress = inProgress;
        this.rejected = rejected;
        this.avgResolutionTimeHours = avgResolutionTimeHours;
        this.categoryCounts = categoryCounts;
    }

    public long getTotalComplaints() { return totalComplaints; }
    public long getResolved() { return resolved; }
    public long getPending() { return pending; }
    public long getInProgress() { return inProgress; }
    public long getRejected() { return rejected; }
    public Double getAvgResolutionTimeHours() { return avgResolutionTimeHours; }
    public Map<String, Long> getCategoryCounts() { return categoryCounts; }
}