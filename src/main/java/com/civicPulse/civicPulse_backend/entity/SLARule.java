package com.civicPulse.civicPulse_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "sla_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sla_category_priority",
                        columnNames = {"category_id", "priority"}
                )
        }
)
public class SLARule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Column(nullable = false)
    private Integer durationHours;


    public SLARule() {}

    public SLARule(Category category, Priority priority, Integer durationHours) {
        this.category = category;
        this.priority = priority;
        this.durationHours = durationHours;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
}