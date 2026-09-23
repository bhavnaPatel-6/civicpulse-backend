package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.Priority;
import com.civicPulse.civicPulse_backend.entity.SLARule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SLARuleRepository extends JpaRepository<SLARule, Long> {

    Optional<SLARule> findByCategoryIdAndPriority(Long categoryId, Priority priority);

    List<SLARule> findAll(); // Admin ke liye - saari rules ek sath dikhane ke liye (already JpaRepository mein hai, explicit likhne ki zaroorat nahi)
}