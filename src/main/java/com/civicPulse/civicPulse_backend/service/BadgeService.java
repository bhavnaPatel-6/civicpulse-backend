package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.ComplaintRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BadgeService {

    private final ComplaintRepository complaintRepository;

    public BadgeService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public List<String> calculateBadges(User citizen) {

        List<String> badges = new ArrayList<>();

        long totalComplaints = complaintRepository
                .findByCitizenId(citizen.getId(), Pageable.unpaged())
                .getTotalElements();

        long accurateComplaints = complaintRepository.countByCitizenIdAndStatusNotIn(
                citizen.getId(),
                List.of(ComplaintStatus.PENDING_VERIFICATION, ComplaintStatus.REJECTED)
        );

        if (totalComplaints >= 25) {
            badges.add("Gold Reporter");
        } else if (totalComplaints >= 10) {
            badges.add("Silver Reporter");
        } else if (totalComplaints >= 3) {
            badges.add("Bronze Reporter");
        }

        if (accurateComplaints >= 5) {
            badges.add("Trusted Citizen");
        }

        if (citizen.getReputationPoints() >= 100) {
            badges.add("Community Hero");
        }

        return badges;
    }
}