package com.civicPulse.civicPulse_backend.dto;

public record ComplaintAssignResponse(
        ComplaintResponse complaint,
        boolean wardMismatch,
        String warningMessage
) {}