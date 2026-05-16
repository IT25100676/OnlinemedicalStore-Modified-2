package com.medistore.service;

import com.medistore.entity.Admin;
import com.medistore.entity.Prescription;
import com.medistore.entity.User;
import com.medistore.entity.enums.PrescriptionStatus;
import com.medistore.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public List<Prescription> findByUser(User user) {
        return prescriptionRepository.findByUserOrderByUploadDateDesc(user);
    }

    public Optional<Prescription> findById(Long id) { return prescriptionRepository.findById(id); }

    public List<Prescription> findAll() { return prescriptionRepository.findAllByOrderByUploadDateDesc(); }

    public List<Prescription> findPending() {
        return prescriptionRepository.findByStatus(PrescriptionStatus.PENDING);
    }

    public long countPending() { return prescriptionRepository.countByStatus(PrescriptionStatus.PENDING); }

    public boolean hasApprovedPrescription(User user) {
        return user != null && prescriptionRepository.existsByUserAndStatus(user, PrescriptionStatus.APPROVED);
    }

    public boolean hasPendingPrescription(User user) {
        return user != null && prescriptionRepository.existsByUserAndStatus(user, PrescriptionStatus.PENDING);
    }

    public boolean hasSubmittedPrescription(User user) {
        return hasApprovedPrescription(user) || hasPendingPrescription(user);
    }

    @Transactional
    public Prescription upload(User user, MultipartFile file, String doctorName, LocalDate issueDate) throws IOException {
        String orig = file.getOriginalFilename();
        String contentType = file.getContentType();

        Prescription p = new Prescription();
        p.setUser(user);
        p.setFileName(orig);
        p.setContentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
        p.setFileSize(file.getSize());
        p.setFileContent(file.getBytes());
        p.setDoctorName(doctorName);
        p.setIssueDate(issueDate);
        return prescriptionRepository.save(p);
    }

    @Transactional
    public Prescription review(Long id, PrescriptionStatus status, String notes, Admin admin) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        p.setStatus(status);
        p.setNotes(notes);
        p.setReviewedBy(admin);
        p.setReviewDate(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }

    @Transactional
    public void delete(Long id) { prescriptionRepository.deleteById(id); }
}
