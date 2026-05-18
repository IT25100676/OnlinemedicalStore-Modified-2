package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Prescription;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.entity.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for Prescription entities.
 *
 * Uses Spring Data JPA — no boilerplate CRUD code needed.
 * Custom queries are derived from method names or via @Query.
 *
 * OOP: Encapsulation — all DB logic is hidden behind this interface.
 *      Callers (PrescriptionService) never write SQL directly.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /** All prescriptions for a specific customer, newest first */
    List<Prescription> findByUserOrderByUploadDateDesc(User user);

    /** All prescriptions with a given status (used for admin review queue) */
    List<Prescription> findByStatus(PrescriptionStatus status);

    /** All prescriptions across all users, newest first (admin view) */
    List<Prescription> findAllByOrderByUploadDateDesc();

    /** Count prescriptions by status (used for dashboard badges) */
    long countByStatus(PrescriptionStatus status);

    /** Check if a customer has at least one prescription in the given status */
    boolean existsByUserAndStatus(User user, PrescriptionStatus status);

    /**
     * Search prescriptions by doctor name or customer username — admin use.
     * Demonstrates a @Query with LIKE for partial matching.
     */
    @Query("SELECT p FROM Prescription p WHERE " +
           "LOWER(p.doctorName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.uploadDate DESC")
    List<Prescription> searchByKeyword(@Param("keyword") String keyword);
}
