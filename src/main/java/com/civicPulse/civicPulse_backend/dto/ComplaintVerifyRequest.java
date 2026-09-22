package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.Priority;
import jakarta.validation.constraints.NotNull;

public class ComplaintVerifyRequest {

    @NotNull(message = "Priority is required")
    private Priority priority;

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}