package com.example.OnlineMedicalStore.entity;

import com.example.OnlineMedicalStore.entity.enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Prescription entity — demonstrates Encapsulation (private fields + getters/setters via Lombok)
 * and Abstraction (business logic methods hidden from callers).
 *
 * OOP Concepts applied:
 *  - Encapsulation  : all fields private, exposed via Lombok @Getter/@Setter
 *  - Abstraction    : isExpired(), isEditable(), getStatusLabel() hide internal logic
 *  - Information hiding : fileContent is LAZY-loaded and never exposed in list views
 */
@Entity
@Table(name = "prescriptions")
@Getter @Setter @NoArgsConstructor
public class Prescription implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Identity ──────────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────
    /** Customer who uploaded this prescription */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Admin who reviewed (approved/rejected) this prescription. Nullable until reviewed. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Admin reviewedBy;

    // ── File metadata ─────────────────────────────────────────────────────────
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Actual file bytes — stored in DB as LONGBLOB.
     * LAZY-fetched to avoid loading large blobs on list pages (Information Hiding).
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_content", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] fileContent;

    // ── Prescription details ──────────────────────────────────────────────────
    @Column(name = "doctor_name")
    private String doctorName;

    /** Date the doctor issued the prescription (entered by customer) */
    @Column(name = "issue_date")
    private LocalDate issueDate;

    /** Timestamp when the customer uploaded the file */
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    /** Current review status: PENDING | APPROVED | REJECTED */
    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    /** Optional admin notes sent back to the customer */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Timestamp when the admin completed their review */
    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }

    // ── Abstraction: business-logic helpers ───────────────────────────────────

    /**
     * A prescription is considered expired if it was issued more than 30 days ago.
     * Demonstrates Abstraction — callers don't need to know the expiry rule.
     */
    public boolean isExpired() {
        if (issueDate == null) return false;
        return ChronoUnit.DAYS.between(issueDate, LocalDate.now()) > 30;
    }

    /**
     * A prescription is editable (deletable) only while PENDING.
     * Customers cannot delete already-reviewed prescriptions.
     */
    public boolean isEditable() {
        return status == PrescriptionStatus.PENDING;
    }

    /**
     * Human-readable status label with icon — used in templates.
     * Demonstrates Abstraction: formatting logic lives here, not in the view.
     */
    public String getStatusLabel() {
        return switch (status) {
            case APPROVED -> "✓ Approved";
            case REJECTED -> "✗ Rejected";
            default       -> "⏳ Pending";
        };
    }

    /**
     * Bootstrap badge CSS class for the current status.
     */
    public String getStatusBadgeClass() {
        return switch (status) {
            case APPROVED -> "bg-success";
            case REJECTED -> "bg-danger";
            default       -> "bg-warning text-dark";
        };
    }

    /**
     * Human-readable file size (e.g. "1.2 MB").
     * Demonstrates Information Hiding — raw bytes are never shown in the UI.
     */
    public String getFileSizeDisplay() {
        if (fileSize == null || fileSize == 0) return "Unknown";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }

    /**
     * Whether this file can be previewed inline in the browser (image or PDF).
     */
    public boolean isPreviewable() {
        if (contentType == null) return false;
        return contentType.startsWith("image/") || contentType.equals("application/pdf");
    }
}
