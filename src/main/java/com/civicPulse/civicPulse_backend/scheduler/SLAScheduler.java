package com.civicPulse.civicPulse_backend.scheduler;

import com.civicPulse.civicPulse_backend.entity.Role;
import com.civicPulse.civicPulse_backend.entity.SLATracker;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.SLATrackerRepository;
import com.civicPulse.civicPulse_backend.repository.UserRepository;
import com.civicPulse.civicPulse_backend.service.EmailService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SLAScheduler {

    private final SLATrackerRepository slaTrackerRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public SLAScheduler(
            SLATrackerRepository slaTrackerRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.slaTrackerRepository = slaTrackerRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 900000) // har 15 min
    public void checkSlaStatus() {

        LocalDateTime now = LocalDateTime.now();

        // ===== Warning - deadline 6 ghante mein aane wali =====
        LocalDateTime warningThreshold = now.plusHours(6);

        List<SLATracker> approaching = slaTrackerRepository
                .findByBreachedFalseAndWarningNotifiedFalseAndDeadlineBefore(warningThreshold);

        for (SLATracker tracker : approaching) {

            var complaint = tracker.getComplaint();
            var authority = complaint.getAssignedAuthority();

            String subject = "⚠️ SLA Deadline Approaching - Complaint #" + complaint.getId();
            String body = "Complaint '" + complaint.getTitle() + "' (Ward: " + complaint.getWard()
                    + ") ki SLA deadline 6 ghante mein aane wali hai. Kripya jaldi karwai karein.";

            if (authority != null) {
                emailService.sendEmail(authority.getEmail(), subject, body);
            }

            notifyAllAdmins(subject, body);

            System.out.println("⚠️ SLA approaching for complaint ID: " + complaint.getId());

            tracker.setWarningNotified(true);
            slaTrackerRepository.save(tracker);
        }

        // ===== Breach - deadline nikal chuki =====
        List<SLATracker> breached = slaTrackerRepository
                .findByBreachedFalseAndDeadlineBefore(now);

        for (SLATracker tracker : breached) {

            var complaint = tracker.getComplaint();
            var authority = complaint.getAssignedAuthority();

            String subject = "🔴 SLA BREACHED - Complaint #" + complaint.getId();
            String body = "Complaint '" + complaint.getTitle() + "' (Ward: " + complaint.getWard()
                    + ") ki SLA deadline nikal chuki hai. Turant karwai zaroori hai. Assigned Authority: "
                    + (authority != null ? authority.getName() : "Not Assigned");

            if (authority != null) {
                emailService.sendEmail(authority.getEmail(), subject, body);
            }

            notifyAllAdmins(subject, body);

            System.out.println("🔴 SLA breached for complaint ID: " + complaint.getId());

            tracker.setBreached(true);
            tracker.setBreachedAt(now);
            slaTrackerRepository.save(tracker);
        }
    }

    // Helper - saare Admins ko email bhejo
    private void notifyAllAdmins(String subject, String body) {

        List<User> admins = userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {
            emailService.sendEmail(admin.getEmail(), subject, body);
        }
    }
}