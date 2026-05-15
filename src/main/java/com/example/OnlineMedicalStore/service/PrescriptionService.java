package com.example.OnlineMedicalStore.service;

import com.example.OnlineMedicalStore.entity.Admin;
import com.example.OnlineMedicalStore.entity.Prescription;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.entity.enums.PrescriptionStatus;
import com.example.OnlineMedicalStore.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Value("${app.upload.dir:uploads/prescriptions}")
    private String uploadDir;

    public List<Prescription> findByUser(User user) {
        return prescriptionRepository.findByUserOrderByUploadDateDesc(user);
    }

    public Optional<Prescription> findById(Long id) { return prescriptionRepository.findById(id); }

    public List<Prescription> findAll() { return prescriptionRepository.findAllByOrderByUploadDateDesc(); }

    public List<Prescription> findPending() {
        return prescriptionRepository.findByStatus(PrescriptionStatus.PENDING);
    }

    public long countPending() { return prescriptionRepository.countByStatus(PrescriptionStatus.PENDING); }

    @Transactional
    public Prescription upload(User user, MultipartFile file, String doctorName, LocalDate issueDate) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String ext = "";
        String orig = file.getOriginalFilename();
        if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf('.'));
        String savedName = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), uploadPath.resolve(savedName), StandardCopyOption.REPLACE_EXISTING);

        Prescription p = new Prescription();
        p.setUser(user);
        p.setFileName(orig);
        p.setFilePath(savedName);
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
