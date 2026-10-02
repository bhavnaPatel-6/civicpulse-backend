package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.AdminStatsResponse;
import com.civicPulse.civicPulse_backend.dto.AuthorityWorkloadResponse;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.Complaint;
import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.entity.Role;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.CategoryRepository;
import com.civicPulse.civicPulse_backend.repository.ComplaintRepository;
import com.civicPulse.civicPulse_backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final ComplaintRepository complaintRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public StatsService(
            ComplaintRepository complaintRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public AdminStatsResponse getStats() {

        long total = complaintRepository.count();
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED)
                + complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING_VERIFICATION)
                + complaintRepository.countByStatus(ComplaintStatus.AUTO_VALIDATED)
                + complaintRepository.countByStatus(ComplaintStatus.NEEDS_EVIDENCE);
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


    // Admin: har authority ke paas kitni complaints hain (status-wise breakdown)
    public List<AuthorityWorkloadResponse> getAuthorityWorkload() {

        List<User> authorities = userRepository.findByRole(Role.AUTHORITY);

        return authorities.stream()
                .map(authority -> new AuthorityWorkloadResponse(
                        authority.getId(),
                        authority.getName(),
                        authority.getDepartment() != null ? authority.getDepartment().name() : null,
                        complaintRepository.countByAssignedAuthorityId(authority.getId()),
                        complaintRepository.countByAssignedAuthorityIdAndStatus(authority.getId(), ComplaintStatus.VERIFIED)
                                + complaintRepository.countByAssignedAuthorityIdAndStatus(authority.getId(), ComplaintStatus.PENDING_VERIFICATION)
                                + complaintRepository.countByAssignedAuthorityIdAndStatus(authority.getId(), ComplaintStatus.AUTO_VALIDATED)
                                + complaintRepository.countByAssignedAuthorityIdAndStatus(authority.getId(), ComplaintStatus.NEEDS_EVIDENCE),
                        complaintRepository.countByAssignedAuthorityIdAndStatus(authority.getId(), ComplaintStatus.IN_PROGRESS),
                        complaintRepository.countByAssignedAuthorityIdAndStatus(authority.getId(), ComplaintStatus.RESOLVED)
                                + complaintRepository.countByAssignedAuthorityIdAndStatus(authority.getId(), ComplaintStatus.CLOSED)
                ))
                .toList();
    }
}