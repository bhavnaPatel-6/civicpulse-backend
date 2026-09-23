package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.SLATracker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SLATrackerRepository extends JpaRepository<SLATracker, Long> {

    Optional<SLATracker> findByComplaintId(Long complaintId);

    // Scheduler ke liye - abhi tak breach nahi hui aisi saari active trackers
    List<SLATracker> findByBreachedFalse();

    // Deadline nazdeek hai par warning abhi nahi bheji - scheduler check karega
    List<SLATracker> findByBreachedFalseAndWarningNotifiedFalseAndDeadlineBefore(LocalDateTime time);

    // Deadline nikal chuki hai par breached flag update nahi hua
    List<SLATracker> findByBreachedFalseAndDeadlineBefore(LocalDateTime time);
}