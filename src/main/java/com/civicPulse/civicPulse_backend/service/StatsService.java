package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.AdminStatsResponse;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.Complaint;
import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.repository.CategoryRepository;
import com.civicPulse.civicPulse_backend.repository.ComplaintRepository;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final ComplaintRepository complaintRepository;
    private final CategoryRepository categoryRepository;

    public StatsService(ComplaintRepository complaintRepository, CategoryRepository categoryRepository) {
        this.complaintRepository = complaintRepository;
        this.categoryRepository = categoryRepository;
    }

    public AdminStatsResponse getStats() {

        long total = complaintRepository.count();
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED)
                + complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING_VERIFICATION);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS)
                + complaintRepository.countByStatus(ComplaintStatus.VERIFIED);
        long rejected = complaintRepository.countByStatus(ComplaintStatus.REJECTED);

        Double avgResolutionHours = calculateAvgResolutionTime();

        Map<String, Long> categoryCounts = new HashMap<>();
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            long count = complaintRepository.countByCategoryId(category.getId());
            categoryCounts.put(category.getName(), count);
        }

        return new AdminStatsResponse(total, resolved, pending, inProgress, rejected, avgResolutionHours, categoryCounts);
    }

    private Double calculateAvgResolutionTime() {

        List<Complaint> resolvedComplaints = complaintRepository.findAllByStatus(ComplaintStatus.RESOLVED);

        if (resolvedComplaints.isEmpty()) {
            return null;
        }

        double totalHours = 0;
        int count = 0;

        for (Complaint c : resolvedComplaints) {
            if (c.getVerifiedAt() != null && c.getResolvedAt() != null) {
                long hours = Duration.between(c.getVerifiedAt(), c.getResolvedAt()).toHours();
                totalHours += hours;
                count++;
            }
        }

        return count > 0 ? Math.round((totalHours / count) * 10.0) / 10.0 : null;
    }
}