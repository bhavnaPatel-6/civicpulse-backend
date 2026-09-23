package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.Priority;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SLARuleCreateRequest {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;


    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
}