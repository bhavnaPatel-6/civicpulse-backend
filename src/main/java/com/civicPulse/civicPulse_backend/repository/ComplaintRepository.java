package com.civicPulse.civicPulse_backend.repository;

import com.civicPulse.civicPulse_backend.entity.Category;
import com.civicPulse.civicPulse_backend.entity.Complaint;
import com.civicPulse.civicPulse_backend.entity.ComplaintStatus;
import com.civicPulse.civicPulse_backend.entity.Department;
import com.civicPulse.civicPulse_backend.entity.Priority;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;          // NEW
import org.springframework.data.repository.query.Param;        // NEW

import java.time.LocalDateTime;                                 // NEW
import java.util.Collection;                                    // NEW
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
    long countByStatus(ComplaintStatus status);
    long countByCategoryId(Long categoryId);

    List<Complaint> findAllByStatus(ComplaintStatus status);
    Page<Complaint> findByAssignedAuthorityId(Long assignedAuthorityId, Pageable pageable);
    long countByAssignedAuthorityIdAndStatus(Long assignedAuthorityId, ComplaintStatus status);

    long countByAssignedAuthorityId(Long assignedAuthorityId);


    // ================= NEW: Priority Engine ke liye =================

    // Density: aas-paas ki same-category open complaints (bounding box)
    @Query("""
        SELECT COUNT(c) FROM Complaint c
        WHERE c.id <> :selfId
          AND c.category.id = :categoryId
          AND c.status IN :statuses
          AND c.latitude  BETWEEN :minLat AND :maxLat
          AND c.longitude BETWEEN :minLng AND :maxLng
        """)
    long countNearbyOpen(@Param("selfId") Long selfId,
                         @Param("categoryId") Long categoryId,
                         @Param("statuses") Collection<ComplaintStatus> statuses,
                         @Param("minLat") double minLat, @Param("maxLat") double maxLat,
                         @Param("minLng") double minLng, @Param("maxLng") double maxLng);

    // Recurrence: same jagah + same category, pichhle N din ki resolved/closed complaints
    @Query("""
        SELECT COUNT(c) FROM Complaint c
        WHERE c.id <> :selfId
          AND c.category.id = :categoryId
          AND c.status IN :statuses
          AND c.createdAt >= :since
          AND c.latitude  BETWEEN :minLat AND :maxLat
          AND c.longitude BETWEEN :minLng AND :maxLng
        """)
    long countNearbySince(@Param("selfId") Long selfId,
                          @Param("categoryId") Long categoryId,
                          @Param("statuses") Collection<ComplaintStatus> statuses,
                          @Param("since") LocalDateTime since,
                          @Param("minLat") double minLat, @Param("maxLat") double maxLat,
                          @Param("minLng") double minLng, @Param("maxLng") double maxLng);
}