package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.PriorityResult;
import com.civicPulse.civicPulse_backend.entity.Complaint;
import com.civicPulse.civicPulse_backend.entity.SensitiveZone;
import com.civicPulse.civicPulse_backend.entity.Priority;
import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.repository.ComplaintRepository;
import com.civicPulse.civicPulse_backend.repository.SensitiveZoneRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Priority Score (0-100) =
 *   Risk Severity        35%
 * + Complaint Density    25%
 * + Location Sensitivity 10%
 * + Complaint Age        15%
 * + Recurrence           15%
 */
@Service
public class PriorityEngine {

    // ---- weights (total = 1.0) ----
    private static final double W_SEVERITY    = 0.35;
    private static final double W_DENSITY     = 0.25;
    private static final double W_SENSITIVITY = 0.10;
    private static final double W_AGE         = 0.15;
    private static final double W_RECURRENCE  = 0.15;

    // ---- caps / settings ----
    private static final double NEARBY_RADIUS_M = 200;
    private static final int    DENSITY_CAP     = 5;   // upvotes + nearby open complaints
    private static final int    AGE_CAP_DAYS    = 7;
    private static final int    RECURRENCE_CAP  = 3;
    private static final int    RECURRENCE_DAYS = 60;
    private static final int    MAX_SEVERITY    = 5;

    // ---- priority bands ----
    // Aapke Priority enum mein sirf LOW / MEDIUM / HIGH hain
    private static final double HIGH_MIN     = 50;
    private static final double MEDIUM_MIN   = 30;

    private static final List<ComplaintStatus> OPEN_STATUSES =
            List.of(ComplaintStatus.PENDING_VERIFICATION, ComplaintStatus.AUTO_VALIDATED, ComplaintStatus.NEEDS_EVIDENCE, ComplaintStatus.VERIFIED, ComplaintStatus.IN_PROGRESS);
    private static final List<ComplaintStatus> DONE_STATUSES =
            List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED);

    private final ComplaintRepository complaintRepository;
    private final SensitiveZoneRepository zoneRepository;

    public PriorityEngine(ComplaintRepository complaintRepository,
                          SensitiveZoneRepository zoneRepository) {
        this.complaintRepository = complaintRepository;
        this.zoneRepository = zoneRepository;
    }

    /** Sirf score nikalta hai, complaint ko change nahi karta. */
    public PriorityResult calculate(Complaint c) {
        double severityScore    = severityScore(c);
        double densityScore     = densityScore(c);
        double sensitivityScore = sensitivityScore(c);
        double ageScore         = ageScore(c);
        double recurrenceScore  = recurrenceScore(c);

        double total = W_SEVERITY    * severityScore
                + W_DENSITY     * densityScore
                + W_SENSITIVITY * sensitivityScore
                + W_AGE         * ageScore
                + W_RECURRENCE  * recurrenceScore;
        total = round(total);

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("severity",    round(W_SEVERITY * severityScore));
        breakdown.put("density",     round(W_DENSITY * densityScore));
        breakdown.put("sensitivity", round(W_SENSITIVITY * sensitivityScore));
        breakdown.put("age",         round(W_AGE * ageScore));
        breakdown.put("recurrence",  round(W_RECURRENCE * recurrenceScore));

        return new PriorityResult(total, toPriority(total), breakdown);
    }

    /** Create / upvote / scheduler: pending, auto-validated aur needs-evidence complaints ki priority update hoti hai. */
    public void recalculate(Complaint c) {
        if (c.getStatus() != ComplaintStatus.PENDING_VERIFICATION
                && c.getStatus() != ComplaintStatus.AUTO_VALIDATED
                && c.getStatus() != ComplaintStatus.NEEDS_EVIDENCE) return;   // verify ke baad freeze
        apply(c);
    }

    /** Verify ke time ek last baar calculate karo; iske baad priority freeze. */
    public void finalizeAtVerification(Complaint c) {
        apply(c);
    }

    private void apply(Complaint c) {
        PriorityResult r = calculate(c);
        c.setPriorityScore(r.score());
        c.setPriority(r.priority());
    }

    // ================= factor scores (each 0-100) =================

    private double severityScore(Complaint c) {
        int sev = Math.max(1, Math.min(MAX_SEVERITY, c.getCategory().getSeverity()));
        return sev / (double) MAX_SEVERITY * 100;
    }

    private double densityScore(Complaint c) {
        double[] box = boundingBox(c.getLatitude(), c.getLongitude(), NEARBY_RADIUS_M);
        long nearbyOpen = complaintRepository.countNearbyOpen(
                idOrMinusOne(c), c.getCategory().getId(), OPEN_STATUSES,
                box[0], box[1], box[2], box[3]);
        long density = c.getUpvoteCount() + nearbyOpen;
        return Math.min(density, DENSITY_CAP) / (double) DENSITY_CAP * 100;
    }

    private double sensitivityScore(Complaint c) {
        for (SensitiveZone z : zoneRepository.findByActiveTrue()) {
            double d = haversineMeters(c.getLatitude(), c.getLongitude(), z.getLatitude(), z.getLongitude());
            if (d <= z.getRadiusMeters()) return 100;
        }
        return 0;
    }

    private double ageScore(Complaint c) {
        if (c.getCreatedAt() == null) return 0;
        double days = Duration.between(c.getCreatedAt(), LocalDateTime.now()).toMinutes() / 1440.0;
        return Math.min(days, AGE_CAP_DAYS) / AGE_CAP_DAYS * 100;
    }

    private double recurrenceScore(Complaint c) {
        double[] box = boundingBox(c.getLatitude(), c.getLongitude(), NEARBY_RADIUS_M);
        long past = complaintRepository.countNearbySince(
                idOrMinusOne(c), c.getCategory().getId(), DONE_STATUSES,
                LocalDateTime.now().minusDays(RECURRENCE_DAYS),
                box[0], box[1], box[2], box[3]);
        return Math.min(past, RECURRENCE_CAP) / (double) RECURRENCE_CAP * 100;
    }

    // ================= helpers =================

    private Priority toPriority(double score) {
        if (score >= HIGH_MIN)     return Priority.HIGH;
        if (score >= MEDIUM_MIN)   return Priority.MEDIUM;
        return Priority.LOW;
    }

    private Long idOrMinusOne(Complaint c) {
        return c.getId() == null ? -1L : c.getId();
    }

    /** returns {minLat, maxLat, minLng, maxLng} */
    private double[] boundingBox(double lat, double lng, double radiusM) {
        double latDelta = radiusM / 111_320.0;
        double lngDelta = radiusM / (111_320.0 * Math.cos(Math.toRadians(lat)));
        return new double[]{lat - latDelta, lat + latDelta, lng - lngDelta, lng + lngDelta};
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}