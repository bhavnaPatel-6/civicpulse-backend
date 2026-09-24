package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.ComplaintRejectRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResolveRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintVerifyRequest;
import com.civicPulse.civicPulse_backend.dto.RewardHistoryResponse;
import com.civicPulse.civicPulse_backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.civicPulse.civicPulse_backend.dto.ComplaintCreateRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResponse;
import com.civicPulse.civicPulse_backend.repository.ComplaintRepository;
import com.civicPulse.civicPulse_backend.repository.ComplaintUpvoteRepository;
import com.civicPulse.civicPulse_backend.repository.RewardHistoryRepository;
import com.civicPulse.civicPulse_backend.repository.SLARuleRepository;
import com.civicPulse.civicPulse_backend.repository.SLATrackerRepository;
import com.civicPulse.civicPulse_backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {

    private static final double NEARBY_RADIUS_METERS = 150;

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final SLARuleRepository slaRuleRepository;
    private final SLATrackerRepository slaTrackerRepository;
    private final ComplaintUpvoteRepository upvoteRepository;
    private final RewardHistoryRepository rewardHistoryRepository;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            CategoryService categoryService,
            SLARuleRepository slaRuleRepository,
            SLATrackerRepository slaTrackerRepository,
            ComplaintUpvoteRepository upvoteRepository,
            RewardHistoryRepository rewardHistoryRepository) {

        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.slaRuleRepository = slaRuleRepository;
        this.slaTrackerRepository = slaTrackerRepository;
        this.upvoteRepository = upvoteRepository;
        this.rewardHistoryRepository = rewardHistoryRepository;
    }


    // Citizen ek naya complaint report karta hai
    public ComplaintResponse createComplaint(
            String citizenEmail,
            ComplaintCreateRequest request) {

        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryService.getCategoryById(request.getCategoryId());

        Complaint complaint = new Complaint();
        complaint.setCitizen(citizen);
        complaint.setCategory(category);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setPhotoUrl(request.getPhotoUrl());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setAddress(request.getAddress());
        complaint.setCity(request.getCity());
        complaint.setWard(request.getWard());

        complaint.setDepartment(category.getDefaultDepartment());

        Complaint saved = complaintRepository.save(complaint);

        return toResponse(saved);
    }


    // Ek specific complaint dekhna
    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        return toResponse(complaint);
    }


    // Citizen: apni saari complaints dekho (paginated)
    public Page<ComplaintResponse> getMyComplaints(String citizenEmail, Pageable pageable) {

        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return complaintRepository.findByCitizenId(citizen.getId(), pageable)
                .map(this::toResponse);
    }


    // Authority: apne department ki saari complaints dekho (paginated)
    public Page<ComplaintResponse> getComplaintsByDepartment(Department department, Pageable pageable) {

        return complaintRepository.findByDepartment(department, pageable)
                .map(this::toResponse);
    }


    // Authority: complaint verify karo, priority set karo
    public ComplaintResponse verifyComplaint(
            String authorityEmail,
            Long complaintId,
            ComplaintVerifyRequest request) {

        User authority = userRepository.findByEmail(authorityEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (authority.getDepartment() != complaint.getDepartment()) {
            throw new RuntimeException("You are not authorized to handle this complaint");
        }

        if (complaint.getStatus() != ComplaintStatus.PENDING_VERIFICATION) {
            throw new RuntimeException("Only pending complaints can be verified");
        }

        complaint.setStatus(ComplaintStatus.VERIFIED);
        complaint.setPriority(request.getPriority());
        complaint.setReviewedBy(authority);
        complaint.setAssignedAuthority(authority);
        complaint.setVerifiedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);

        createSlaTracker(saved);

        awardPoints(saved.getCitizen(), 10, RewardReason.COMPLAINT_VERIFIED, saved);

        return toResponse(saved);
    }


    // Authority: complaint reject karo, reason ke saath
    public ComplaintResponse rejectComplaint(
            String authorityEmail,
            Long complaintId,
            ComplaintRejectRequest request) {

        User authority = userRepository.findByEmail(authorityEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (authority.getDepartment() != complaint.getDepartment()) {
            throw new RuntimeException("You are not authorized to handle this complaint");
        }
        if (complaint.getStatus() != ComplaintStatus.PENDING_VERIFICATION) {
            throw new RuntimeException("Only pending complaints can be rejected");
        }

        complaint.setStatus(ComplaintStatus.REJECTED);
        complaint.setRejectionReason(request.getReason());
        complaint.setReviewedBy(authority);

        Complaint saved = complaintRepository.save(complaint);

        return toResponse(saved);
    }


    // Authority: verified complaint pe kaam shuru karo
    public ComplaintResponse startProgress(String authorityEmail, Long complaintId) {

        User authority = userRepository.findByEmail(authorityEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getAssignedAuthority() == null
                || !complaint.getAssignedAuthority().getId().equals(authority.getId())) {
            throw new RuntimeException("This complaint is not assigned to you");
        }

        if (complaint.getStatus() != ComplaintStatus.VERIFIED) {
            throw new RuntimeException("Only VERIFIED complaints can be moved to IN_PROGRESS");
        }

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        return toResponse(complaintRepository.save(complaint));
    }


    // Authority: complaint resolve karo, proof photo ke saath
    public ComplaintResponse resolveComplaint(
            String authorityEmail,
            Long complaintId,
            ComplaintResolveRequest request) {

        User authority = userRepository.findByEmail(authorityEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getAssignedAuthority() == null
                || !complaint.getAssignedAuthority().getId().equals(authority.getId())) {
            throw new RuntimeException("This complaint is not assigned to you");
        }

        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new RuntimeException("Only IN_PROGRESS complaints can be resolved");
        }

        if (request.getResolutionPhotoUrl() == null || request.getResolutionPhotoUrl().isBlank()) {
            throw new RuntimeException("Resolution proof photo is required");
        }

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolutionNote(request.getResolutionNote());
        complaint.setResolutionPhotoUrl(request.getResolutionPhotoUrl());
        complaint.setResolvedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);

        awardPoints(saved.getCitizen(), 15, RewardReason.COMPLAINT_RESOLVED, saved);

        return toResponse(saved);
    }


    // Citizen: resolved complaint ko confirm/close karo
    public ComplaintResponse closeComplaint(String citizenEmail, Long complaintId) {

        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new RuntimeException("This is not your complaint");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new RuntimeException("Only RESOLVED complaints can be closed");
        }

        complaint.setStatus(ComplaintStatus.CLOSED);

        return toResponse(complaintRepository.save(complaint));
    }


    // ===== Similar complaints dhoondo (radius-based) =====
    public List<ComplaintResponse> findSimilarComplaints(Long categoryId, Double latitude, Double longitude) {

        List<ComplaintStatus> excluded = List.of(ComplaintStatus.CLOSED, ComplaintStatus.REJECTED);

        List<Complaint> sameCategory = complaintRepository
                .findByCategoryIdAndStatusNotIn(categoryId, excluded);

        return sameCategory.stream()
                .filter(c -> calculateDistanceInMeters(
                        latitude, longitude, c.getLatitude(), c.getLongitude()) <= NEARBY_RADIUS_METERS)
                .map(this::toResponse)
                .toList();
    }


    // ===== Upvote karo =====
    public ComplaintResponse upvoteComplaint(String citizenEmail, Long complaintId) {

        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new RuntimeException("Cannot upvote a closed or rejected complaint");
        }

        if (complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new RuntimeException("You cannot upvote your own complaint");
        }

        boolean alreadyUpvoted = upvoteRepository
                .existsByComplaintIdAndCitizenId(complaintId, citizen.getId());

        if (alreadyUpvoted) {
            throw new RuntimeException("You have already upvoted this complaint");
        }

        ComplaintUpvote upvote = new ComplaintUpvote(complaint, citizen);
        upvoteRepository.save(upvote);

        complaint.setUpvoteCount(complaint.getUpvoteCount() + 1);

        if (complaint.getUpvoteCount() >= 10 && complaint.getPriority() == null) {
            complaint.setPriority(Priority.HIGH);
        }

        Complaint saved = complaintRepository.save(complaint);

        awardPoints(citizen, 2, RewardReason.UPVOTE_GIVEN, saved);

        if (saved.getUpvoteCount() == 5) {
            awardPoints(saved.getCitizen(), 20, RewardReason.POPULAR_COMPLAINT_BONUS, saved);
        }

        return toResponse(saved);
    }


    // ===== Apni reward history dekho =====
    public Page<RewardHistoryResponse> getMyRewardHistory(String citizenEmail, Pageable pageable) {

        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return rewardHistoryRepository.findByCitizenIdOrderByCreatedAtDesc(citizen.getId(), pageable)
                .map(rh -> new RewardHistoryResponse(
                        rh.getId(),
                        rh.getPoints(),
                        rh.getReason(),
                        rh.getComplaint() != null ? rh.getComplaint().getId() : null,
                        rh.getComplaint() != null ? rh.getComplaint().getTitle() : null,
                        rh.getCreatedAt()
                ));
    }


    // Helper - points award karo aur history save karo
    private void awardPoints(User citizen, int points, RewardReason reason, Complaint complaint) {

        citizen.setReputationPoints(citizen.getReputationPoints() + points);
        userRepository.save(citizen);

        RewardHistory history = new RewardHistory(citizen, points, reason, complaint);
        rewardHistoryRepository.save(history);
    }


    // Helper - SLA rule dekh kar tracker banata hai
    private void createSlaTracker(Complaint complaint) {

        SLARule rule = slaRuleRepository
                .findByCategoryIdAndPriority(complaint.getCategory().getId(), complaint.getPriority())
                .orElseThrow(() -> new RuntimeException(
                        "SLA rule not defined for this category and priority. Contact admin."));

        SLATracker tracker = new SLATracker(
                complaint,
                complaint.getVerifiedAt(),
                complaint.getVerifiedAt().plusHours(rule.getDurationHours())
        );

        slaTrackerRepository.save(tracker);
    }


    // Helper - Haversine formula, do points ke beech distance nikalta hai (meters mein)
    private double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }


    // Entity ko Response DTO mein convert karna
    private ComplaintResponse toResponse(Complaint c) {
        return new ComplaintResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getCategory().getName(),
                c.getPhotoUrl(),
                c.getLatitude(),
                c.getLongitude(),
                c.getAddress(),
                c.getCity(),
                c.getWard(),
                c.getDepartment(),
                c.getPriority(),
                c.getStatus(),
                c.getCitizen().getName(),
                c.getAssignedAuthority() != null ? c.getAssignedAuthority().getName() : null,
                c.getRejectionReason(),
                c.getResolutionNote(),
                c.getResolutionPhotoUrl(),
                c.getUpvoteCount(),
                c.getCreatedAt(),
                c.getVerifiedAt(),
                c.getResolvedAt()
        );
    }
}