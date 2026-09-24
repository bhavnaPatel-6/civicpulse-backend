package com.civicPulse.civicPulse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_history")
public class RewardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RewardReason reason;

    // Optional - kis complaint ki wajah se mila (null ho sakta hai future generic rewards ke liye)
    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    public RewardHistory() {}

    public RewardHistory(User citizen, Integer points, RewardReason reason, Complaint complaint) {
        this.citizen = citizen;
        this.points = points;
        this.reason = reason;
        this.complaint = complaint;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCitizen() { return citizen; }
    public void setCitizen(User citizen) { this.citizen = citizen; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public RewardReason getReason() { return reason; }
    public void setReason(RewardReason reason) { this.reason = reason; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}