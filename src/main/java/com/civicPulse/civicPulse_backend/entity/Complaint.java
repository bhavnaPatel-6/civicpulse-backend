package com.civicPulse.civicPulse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kis citizen ne complaint report ki
    @ManyToOne
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    // Complaint category
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Complaint title
    @Column(nullable = false, length = 150)
    private String title;

    // Complaint description
    @Column(length = 2000)
    private String description;

    // Image ka URL
    private String photoUrl;

    // Exact GPS location
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Human-readable address
    @Column(length = 500)
    private String address;

    // Area information
    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String ward;

    // Category ke default department se set hoga
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Department department;

    // Authority verification ke baad priority
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority;

    // Complaint ka current status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status;

    // Currently assigned authority
    @ManyToOne
    @JoinColumn(name = "assigned_authority_id")
    private User assignedAuthority;

    // Kis authority ne review kiya
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    // Agar complaint reject hui
    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime verifiedAt;

    private LocalDateTime resolvedAt;


    // ===== Lifecycle Methods =====

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = ComplaintStatus.PENDING_VERIFICATION;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
// Complaint.java mein add karo

    @Column(length = 1000)
    private String resolutionNote;

    private String resolutionPhotoUrl;

    // Getters and Setters
    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public String getResolutionPhotoUrl() {
        return resolutionPhotoUrl;
    }

    public void setResolutionPhotoUrl(String resolutionPhotoUrl) {
        this.resolutionPhotoUrl = resolutionPhotoUrl;
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCitizen() {
        return citizen;
    }

    public void setCitizen(User citizen) {
        this.citizen = citizen;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public User getAssignedAuthority() {
        return assignedAuthority;
    }

    public void setAssignedAuthority(User assignedAuthority) {
        this.assignedAuthority = assignedAuthority;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}