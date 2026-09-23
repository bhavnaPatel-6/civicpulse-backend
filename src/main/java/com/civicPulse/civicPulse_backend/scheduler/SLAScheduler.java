package com.civicPulse.civicPulse_backend.scheduler;

import com.civicPulse.civicPulse_backend.entity.SLATracker;
import com.civicPulse.civicPulse_backend.repository.SLATrackerRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SLAScheduler {

    private final SLATrackerRepository slaTrackerRepository;

    public SLAScheduler(SLATrackerRepository slaTrackerRepository) {
        this.slaTrackerRepository = slaTrackerRepository;
    }

    @Scheduled(fixedRate = 900000) // har 15 min
    public void checkSlaStatus() {

        LocalDateTime now = LocalDateTime.now();

        // Warning - deadline 6 ghante mein aane wali
        LocalDateTime warningThreshold = now.plusHours(6);

        List<SLATracker> approaching = slaTrackerRepository
                .findByBreachedFalseAndWarningNotifiedFalseAndDeadlineBefore(warningThreshold);

        for (SLATracker tracker : approaching) {
            System.out.println("⚠️ SLA approaching for complaint ID: "
                    + tracker.getComplaint().getId());

            tracker.setWarningNotified(true);
            slaTrackerRepository.save(tracker);
        }

        // Breach - deadline nikal chuki
        List<SLATracker> breached = slaTrackerRepository
                .findByBreachedFalseAndDeadlineBefore(now);

        for (SLATracker tracker : breached) {
            System.out.println("🔴 SLA breached for complaint ID: "
                    + tracker.getComplaint().getId());

            tracker.setBreached(true);
            tracker.setBreachedAt(now);
            slaTrackerRepository.save(tracker);
        }
    }
}