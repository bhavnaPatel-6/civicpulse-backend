package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.RewardReason;

import java.time.LocalDateTime;

public class RewardHistoryResponse {

    private Long id;
    private Integer points;
    private RewardReason reason;
    private Long complaintId;
    private String complaintTitle;
    private LocalDateTime createdAt;

    public RewardHistoryResponse(
            Long id, Integer points, RewardReason reason,
            Long complaintId, String complaintTitle, LocalDateTime createdAt) {
        this.id = id;
        this.points = points;
        this.reason = reason;
        this.complaintId = complaintId;
        this.complaintTitle = complaintTitle;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Integer getPoints() { return points; }
    public RewardReason getReason() { return reason; }
    public Long getComplaintId() { return complaintId; }
    public String getComplaintTitle() { return complaintTitle; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}