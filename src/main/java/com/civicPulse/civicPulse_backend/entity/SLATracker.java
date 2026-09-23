package com.civicPulse.civicPulse_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_trackers")
public class SLATracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ek complaint ka sirf ek hi tracker hoga
    @OneToOne
    @JoinColumn(name = "complaint_id", nullable = false, unique = true)
    private Complaint complaint;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private Boolean breached = false;

    // Deadline paas aane ki warning bhej di gayi kya (duplicate notification na jaye)
    @Column(nullable = false)
    private Boolean warningNotified = false;

    private LocalDateTime breachedAt;


    public SLATracker() {}

    public SLATracker(Complaint complaint, LocalDateTime startedAt, LocalDateTime deadline) {
        this.complaint = complaint;
        this.startedAt = startedAt;
        this.deadline = deadline;
        this.breached = false;
        this.warningNotified = false;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Boolean getBreached() { return breached; }
    public void setBreached(Boolean breached) { this.breached = breached; }

    public Boolean getWarningNotified() { return warningNotified; }
    public void setWarningNotified(Boolean warningNotified) { this.warningNotified = warningNotified; }

    public LocalDateTime getBreachedAt() { return breachedAt; }
    public void setBreachedAt(LocalDateTime breachedAt) { this.breachedAt = breachedAt; }
}