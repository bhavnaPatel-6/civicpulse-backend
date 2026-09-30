package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.SensitiveZone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensitiveZoneRepository extends JpaRepository<SensitiveZone, Long> {
    List<SensitiveZone> findByActiveTrue();
}