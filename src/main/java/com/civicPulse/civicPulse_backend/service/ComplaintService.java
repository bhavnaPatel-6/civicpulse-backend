package com.civicPulse.civicPulse_backend.service;

import com.civicPulse.civicPulse_backend.dto.ComplaintCreateRequest;
import com.civicPulse.civicPulse_backend.dto.ComplaintResponse;
import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.Complaint;
import com.civicPulse.civicPulse_backend.entity.User;
import com.civicPulse.civicPulse_backend.repository.ComplaintRepository;
import com.civicPulse.civicPulse_backend.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            CategoryService categoryService) {

        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }


    // Citizen ek naya complaint report karta hai
    public ComplaintResponse createComplaint(
            String citizenEmail,
            ComplaintCreateRequest request) {

        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ye already exist + active dono check karta hai (pichli file mein update kiya tha)
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

        // Category ke default department se auto-assign
        complaint.setDepartment(category.getDefaultDepartment());

        // status = PENDING_VERIFICATION already @PrePersist mein set ho jayega

        Complaint saved = complaintRepository.save(complaint);

        return toResponse(saved);
    }


    // Ek specific complaint dekhna (citizen apni, authority apne department ki)
    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        return toResponse(complaint);
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
                c.getCreatedAt(),
                c.getVerifiedAt(),
                c.getResolvedAt()
        );
    }
}