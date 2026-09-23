package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.ComplaintRejectRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResolveRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintVerifyRequest;
import com.civicPulse.civicPulse_backend.entity.*;
import com.civicPulse.civicPulse_backend.repository.SLARuleRepository;
import com.civicPulse.civicPulse_backend.repository.SLATrackerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.civicPulse.civicPulse_backend.dto.ComplaintCreateRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResponse;
import com.civicPulse.civicPulse_backend.repository.ComplaintRepository;
import com.civicPulse.civicPulse_backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

// ComplaintService.java mein constructor + fields update karo

    private final SLARuleRepository slaRuleRepository;
    private final SLATrackerRepository slaTrackerRepository;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            CategoryService categoryService,
            SLARuleRepository slaRuleRepository,
            SLATrackerRepository slaTrackerRepository) {

        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.slaRuleRepository = slaRuleRepository;
        this.slaTrackerRepository = slaTrackerRepository;
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
// verifyComplaint() method ke andar, saved hone ke turant baad ye add karo:

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

        // ===== SLA Tracker create karo =====
        createSlaTracker(saved);

        // Citizen ko reputation points
        User citizen = saved.getCitizen();
        citizen.setReputationPoints(citizen.getReputationPoints() + 10);
        userRepository.save(citizen);

        return toResponse(saved);
    }

    // Naya private helper method - class ke neeche add karo
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

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolutionNote(request.getResolutionNote());
        complaint.setResolutionPhotoUrl(request.getResolutionPhotoUrl());
        complaint.setResolvedAt(LocalDateTime.now());

        return toResponse(complaintRepository.save(complaint));
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
                c.getCreatedAt(),
                c.getVerifiedAt(),
                c.getResolvedAt()
        );
    }
}