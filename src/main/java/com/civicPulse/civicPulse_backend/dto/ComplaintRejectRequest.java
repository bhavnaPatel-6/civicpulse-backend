package com.civicPulse.civicPulse_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComplaintRejectRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Reason must be under 500 characters")
    private String reason;


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}