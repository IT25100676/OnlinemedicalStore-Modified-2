package com.example.OnlineMedicalStore.service;

import com.example.OnlineMedicalStore.entity.Admin;
import com.example.OnlineMedicalStore.entity.Prescription;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.entity.enums.PrescriptionStatus;
import com.example.OnlineMedicalStore.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Prescription Management — all business logic lives here.
 *
 * OOP Concepts demonstrated:
 *  - Encapsulation  : Business rules are encapsulated inside methods; controllers
 *                     call simple methods without knowing the implementation.
 *  - Abstraction    : upload(), review(), delete() hide file I/O, validation,
 *                     and DB operations behind clean method signatures.
 *  - Polymorphism   : applyValidationRules() behaves differently at runtime
 *                     depending on the PrescriptionStatus passed in.
 */
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    // Allowed MIME types for uploaded prescription files
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    // Maximum file size: 10 MB
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private final PrescriptionRepository prescriptionRepository;

    // ── READ operations ───────────────────────────────────────────────────────

    /** Find all prescriptions belonging to a specific customer */
    public List<Prescription> findByUser(User user) {
        return prescriptionRepository.findByUserOrderByUploadDateDesc(user);
    }

    /** Find a single prescription by its ID */
    public Optional<Prescription> findById(Long id) {
        return prescriptionRepository.findById(id);
    }

    /** Find all prescriptions (admin view — all customers) */
    public List<Prescription> findAll() {
        return prescriptionRepository.findAllByOrderByUploadDateDesc();
    }

    /** Find prescriptions currently awaiting admin review */
    public List<Prescription> findPending() {
        return prescriptionRepository.findByStatus(PrescriptionStatus.PENDING);
    }

    /** Count prescriptions with PENDING status — used for admin dashboard badge */
    public long countPending() {
        return prescriptionRepository.countByStatus(PrescriptionStatus.PENDING);
    }

    /**
     * Search prescriptions by doctor name or customer username.
     * Demonstrates the Read operation with filtering/search.
     */
    public List<Prescription> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return prescriptionRepository.findAllByOrderByUploadDateDesc();
        }
        return prescriptionRepository.searchByKeyword(keyword.trim());
    }

    // ── Business-rule helpers ─────────────────────────────────────────────────

    /** Returns true if the customer has an APPROVED prescription on file */
    public boolean hasApprovedPrescription(User user) {
        return user != null &&
               prescriptionRepository.existsByUserAndStatus(user, PrescriptionStatus.APPROVED);
    }

    /** Returns true if the customer has a PENDING prescription on file */
    public boolean hasPendingPrescription(User user) {
        return user != null &&
               prescriptionRepository.existsByUserAndStatus(user, PrescriptionStatus.PENDING);
    }

    /** Returns true if the customer has either APPROVED or PENDING prescription */
    public boolean hasSubmittedPrescription(User user) {
        return hasApprovedPrescription(user) || hasPendingPrescription(user);
    }

    // ── CREATE operation ──────────────────────────────────────────────────────

    /**
     * Upload a new prescription file for a customer.
     *
     * Demonstrates Abstraction: caller just passes a MultipartFile;
     * this method handles all validation, conversion, and persistence.
     *
     * @param user       The logged-in customer
     * @param file       The uploaded file (PDF or image)
     * @param doctorName Name of the prescribing doctor (optional)
     * @param issueDate  Date the prescription was issued (optional)
     * @return The saved Prescription entity
     * @throws IOException              If file reading fails
     * @throws IllegalArgumentException If file type or size is invalid
     */
    @Transactional
    public Prescription upload(User user, MultipartFile file,
                               String doctorName, LocalDate issueDate) throws IOException {

        // --- Validation (Abstraction: rules hidden inside service) ---
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must not exceed 10 MB.");
        }
        String detectedType = file.getContentType();
        if (detectedType == null || !ALLOWED_TYPES.contains(detectedType)) {
            throw new IllegalArgumentException(
                "Invalid file type. Only PDF, JPG, and PNG are accepted."
            );
        }

        // --- Build entity ---
        Prescription p = new Prescription();
        p.setUser(user);
        p.setFileName(file.getOriginalFilename());
        p.setContentType(detectedType);
        p.setFileSize(file.getSize());
        p.setFileContent(file.getBytes());
        p.setDoctorName(doctorName != null && !doctorName.isBlank() ? doctorName.trim() : null);
        p.setIssueDate(issueDate);

        return prescriptionRepository.save(p);
    }

    // ── UPDATE operation ──────────────────────────────────────────────────────

    /**
     * Admin reviews a prescription — approve, reject, or keep pending.
     *
     * Demonstrates Polymorphism: applyValidationRules() applies different
     * logic at runtime depending on the target status.
     *
     * @param id     Prescription ID
     * @param status New status (APPROVED | REJECTED | PENDING)
     * @param notes  Optional admin notes for the customer
     * @param admin  The admin performing the review
     */
    @Transactional
    public Prescription review(Long id, PrescriptionStatus status,
                               String notes, Admin admin) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription #" + id + " not found."));

        // Polymorphic validation — different rules per status
        applyValidationRules(status, notes, p);

        p.setStatus(status);
        p.setNotes(notes != null && !notes.isBlank() ? notes.trim() : null);
        p.setReviewedBy(admin);
        p.setReviewDate(LocalDateTime.now());

        return prescriptionRepository.save(p);
    }

    /**
     * Polymorphism in action: the same method name applies different
     * validation logic at runtime based on the status value.
     *
     * - APPROVED → check the prescription is not expired
     * - REJECTED → notes are required so the customer knows why
     * - PENDING  → no extra rules
     */
    private void applyValidationRules(PrescriptionStatus status,
                                      String notes, Prescription p) {
        switch (status) {
            case APPROVED -> {
                if (p.isExpired()) {
                    throw new IllegalStateException(
                        "Cannot approve: this prescription was issued more than 30 days ago."
                    );
                }
            }
            case REJECTED -> {
                if (notes == null || notes.isBlank()) {
                    throw new IllegalArgumentException(
                        "Please provide a reason for rejection in the Notes field."
                    );
                }
            }
            case PENDING -> { /* no additional rules */ }
        }
    }

    // ── DELETE operation ──────────────────────────────────────────────────────

    /**
     * Delete a prescription.
     *
     * Business rule: only PENDING prescriptions can be deleted by customers;
     * admins can delete any prescription.
     *
     * @param id          Prescription ID to delete
     * @param requestUser The user requesting deletion
     * @param isAdmin     True if the requester is an admin
     */
    @Transactional
    public void delete(Long id, User requestUser, boolean isAdmin) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription #" + id + " not found."));

        if (!isAdmin) {
            // Customers can only delete their own PENDING prescriptions
            if (!p.getUser().getId().equals(requestUser.getId())) {
                throw new SecurityException("You can only delete your own prescriptions.");
            }
            if (!p.isEditable()) {
                throw new IllegalStateException(
                    "You can only delete prescriptions that are still pending review."
                );
            }
        }
        prescriptionRepository.deleteById(id);
    }
}
