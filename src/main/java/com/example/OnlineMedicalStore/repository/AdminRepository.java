package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Admin;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepository extends AbstractTextFileRepository<Admin> {

    public AdminRepository() {
        super("admins.txt");
    }

    @Override
    public <S extends Admin> S save(S admin) {
        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(LocalDateTime.now());
        }
        return super.save(admin);
    }

    public Optional<Admin> findByUsername(String username) {
        String value = lower(username);
        return findAll().stream()
                .filter(admin -> lower(admin.getUsername()).equals(value))
                .findFirst();
    }

    public Optional<Admin> findByEmail(String email) {
        String value = lower(email);
        return findAll().stream()
                .filter(admin -> lower(admin.getEmail()).equals(value))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public List<Admin> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email) {
        String usernameValue = lower(username);
        String emailValue = lower(email);
        return findAll().stream()
                .filter(admin -> lower(admin.getUsername()).contains(usernameValue)
                        || lower(admin.getEmail()).contains(emailValue))
                .toList();
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
