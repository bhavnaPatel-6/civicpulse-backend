package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.Complaint;
import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.entity.Department;
import com.civicPulse.civicPulse_backend.entity.Priority;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {

    // Citizen ki apni complaints
    Page<Complaint> findByCitizenId(
            Long citizenId,
            Pageable pageable
    );

    // Authority ke department ki complaints
    Page<Complaint> findByDepartment(
            Department department,
            Pageable pageable
    );

    // Department + status filter
    Page<Complaint> findByDepartmentAndStatus(
            Department department,
            ComplaintStatus status,
            Pageable pageable
    );

    // Admin - status ke according complaints
    Page<Complaint> findByStatus(
            ComplaintStatus status,
            Pageable pageable
    );

    // City + Ward filtering
    List<Complaint> findByCityAndWard(
            String city,
            String ward
    );

    // Category-wise complaint count
    long countByCategory(Category category);

    // Department + Priority ke according
    // oldest complaints first
    Page<Complaint> findByDepartmentAndPriorityOrderByCreatedAtAsc(
            Department department,
            Priority priority,
            Pageable pageable
    );

    // SLA scheduler ke liye
    List<Complaint> findByStatus(
            ComplaintStatus status
    );

    // Authority ne kitni complaints review ki
    long countByReviewedById(
            Long reviewedById
    );


    // Citizen + particular status
    List<Complaint> findByCitizenIdAndStatus(
            Long citizenId,
            ComplaintStatus status
    );

    // ComplaintRepository mein ek naya method add karo:
    long countByCitizenIdAndStatusNot(Long citizenId, ComplaintStatus status);
// ComplaintRepository.java mein add karo
long countByCitizenIdAndStatusNotIn(Long citizenId, List<ComplaintStatus> excludedStatuses);
    List<Complaint> findByCategoryIdAndCityAndWardAndStatusNotIn(
            Long categoryId, String city, String ward, List<ComplaintStatus> excludedStatuses);
    List<Complaint> findByCategoryIdAndStatusNotIn(Long categoryId, List<ComplaintStatus> excludedStatuses);
}

