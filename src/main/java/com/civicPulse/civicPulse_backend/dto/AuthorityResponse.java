package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.Department;

public record AuthorityResponse(
        Long id,
        String name,
        String email,
        String ward,
        Department department
) {}