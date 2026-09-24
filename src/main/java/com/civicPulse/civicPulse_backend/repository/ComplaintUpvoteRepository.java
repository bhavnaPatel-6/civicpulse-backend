package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.ComplaintUpvote;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintUpvoteRepository extends JpaRepository<ComplaintUpvote, Long> {

    boolean existsByComplaintIdAndCitizenId(Long complaintId, Long citizenId);

    long countByComplaintId(Long complaintId);
}