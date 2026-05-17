package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByUsernameIgnoreCase(String username);

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Admin> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
}
