package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.RewardHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

    Page<RewardHistory> findByCitizenIdOrderByCreatedAtDesc(Long citizenId, Pageable pageable);
}