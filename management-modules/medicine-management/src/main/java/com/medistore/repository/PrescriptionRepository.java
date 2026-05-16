package com.medistore.repository;

import com.medistore.entity.Prescription;
import com.medistore.entity.User;
import com.medistore.entity.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByUserOrderByUploadDateDesc(User user);

    List<Prescription> findByStatus(PrescriptionStatus status);

    List<Prescription> findAllByOrderByUploadDateDesc();

    long countByStatus(PrescriptionStatus status);

    boolean existsByUserAndStatus(User user, PrescriptionStatus status);
}
