package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    // UserRepository.java mein add karo
    List<User> findTop10ByOrderByReputationPointsDesc();
}