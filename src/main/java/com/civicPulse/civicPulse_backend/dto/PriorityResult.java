package com.civicPulse.civicPulse_backend.dto;

import com.civicPulse.civicPulse_backend.entity.Priority;
import java.util.Map;

public record PriorityResult(double score, Priority priority, Map<String, Double> breakdown) {}