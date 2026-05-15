package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Prescription;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.entity.enums.PrescriptionStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Repository
public class PrescriptionRepository extends AbstractTextFileRepository<Prescription> {

    public PrescriptionRepository() {
        super("prescriptions.txt");
    }

    @Override
    public Prescription save(Prescription prescription) {
        if (prescription.getUploadDate() == null) {
            prescription.setUploadDate(LocalDateTime.now());
        }
        return super.save(prescription);
    }

    public List<Prescription> findByUserOrderByUploadDateDesc(User user) {
        Long userId = user != null ? user.getId() : null;
        String userEmail = lower(user != null ? user.getEmail() : null);
        String username = lower(user != null ? user.getUsername() : null);
        return findAllByOrderByUploadDateDesc().stream()
                .filter(prescription -> belongsToUser(prescription, userId, userEmail, username))
                .toList();
    }

    private boolean belongsToUser(Prescription prescription, Long userId, String userEmail, String username) {
        User prescriptionUser = prescription.getUser();
        if (prescriptionUser == null) {
            return false;
        }
        if (userId != null && userId.equals(prescriptionUser.getId())) {
            return true;
        }
        if (!userEmail.isBlank() && userEmail.equals(lower(prescriptionUser.getEmail()))) {
            return true;
        }
        return !username.isBlank() && username.equals(lower(prescriptionUser.getUsername()));
    }

    public List<Prescription> findByStatus(PrescriptionStatus status) {
        return findAll().stream()
                .filter(prescription -> prescription.getStatus() == status)
                .toList();
    }

    public List<Prescription> findAllByOrderByUploadDateDesc() {
        return findAll().stream()
                .sorted(Comparator.comparing(Prescription::getUploadDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public long countByStatus(PrescriptionStatus status) {
        return findByStatus(status).size();
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
