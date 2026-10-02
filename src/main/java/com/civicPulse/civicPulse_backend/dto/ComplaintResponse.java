package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.entity.Department;
import com.civicPulse.civicPulse_backend.entity.Priority;

import java.time.LocalDateTime;

public class ComplaintResponse {

    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String ward;
    private Department department;
    private Priority priority;
    private ComplaintStatus status;
    private String citizenName;
    private String assignedAuthorityName;
    private String rejectionReason;
    private String resolutionNote;
    private String resolutionPhotoUrl;
    private Integer upvoteCount;
    private LocalDateTime slaDeadline;
    private Boolean slaBreached;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime resolvedAt;
    private Boolean aiValidated;
    private String aiValidationReason;

    public ComplaintResponse(
            Long id,
            String title,
            String description,
            String categoryName,
            String photoUrl,
            Double latitude,
            Double longitude,
            String address,
            String city,
            String ward,
            Department department,
            Priority priority,
            ComplaintStatus status,
            String citizenName,
            String assignedAuthorityName,
            String rejectionReason,
            String resolutionNote,
            String resolutionPhotoUrl,
            Integer upvoteCount,
            LocalDateTime slaDeadline,
            Boolean slaBreached,
            LocalDateTime createdAt,
            LocalDateTime verifiedAt,
            LocalDateTime resolvedAt,
            Boolean aiValidated,
            String aiValidationReason) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryName = categoryName;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.ward = ward;
        this.department = department;
        this.priority = priority;
        this.status = status;
        this.citizenName = citizenName;
        this.assignedAuthorityName = assignedAuthorityName;
        this.rejectionReason = rejectionReason;
        this.resolutionNote = resolutionNote;
        this.resolutionPhotoUrl = resolutionPhotoUrl;
        this.upvoteCount = upvoteCount;
        this.slaDeadline = slaDeadline;
        this.slaBreached = slaBreached;
        this.createdAt = createdAt;
        this.verifiedAt = verifiedAt;
        this.resolvedAt = resolvedAt;
        this.aiValidated = aiValidated;
        this.aiValidationReason = aiValidationReason;
    }

    public ComplaintResponse(
            Long id,
            String title,
            String description,
            String categoryName,
            String photoUrl,
            Double latitude,
            Double longitude,
            String address,
            String city,
            String ward,
            Department department,
            Priority priority,
            ComplaintStatus status,
            String citizenName,
            String assignedAuthorityName,
            String rejectionReason,
            String resolutionNote,
            String resolutionPhotoUrl,
            Integer upvoteCount,
            LocalDateTime slaDeadline,
            Boolean slaBreached,
            LocalDateTime createdAt,
            LocalDateTime verifiedAt,
            LocalDateTime resolvedAt) {

        this(id, title, description, categoryName, photoUrl, latitude, longitude, address, city, ward,
             department, priority, status, citizenName, assignedAuthorityName, rejectionReason, resolutionNote,
             resolutionPhotoUrl, upvoteCount, slaDeadline, slaBreached, createdAt, verifiedAt, resolvedAt,
             null, null);
    }

    // ===== Getters =====

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategoryName() { return categoryName; }
    public String getPhotoUrl() { return photoUrl; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getWard() { return ward; }
    public Department getDepartment() { return department; }
    public Priority getPriority() { return priority; }
    public ComplaintStatus getStatus() { return status; }
    public String getCitizenName() { return citizenName; }
    public String getAssignedAuthorityName() { return assignedAuthorityName; }
    public String getRejectionReason() { return rejectionReason; }
    public String getResolutionNote() { return resolutionNote; }
    public String getResolutionPhotoUrl() { return resolutionPhotoUrl; }
    public Integer getUpvoteCount() { return upvoteCount; }
    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public Boolean getSlaBreached() { return slaBreached; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public Boolean getAiValidated() { return aiValidated; }
    public String getAiValidationReason() { return aiValidationReason; }
}