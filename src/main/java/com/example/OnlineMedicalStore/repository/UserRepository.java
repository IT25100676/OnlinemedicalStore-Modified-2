package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.entity.Admin;
import com.example.OnlineMedicalStore.entity.Customer;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends AbstractTextFileRepository<User> {

    public UserRepository() {
        super("users.txt");
    }

    @Override
    public <S extends User> S save(S user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        return super.save(user);
    }

    public Optional<User> findByUsername(String username) {
        String value = lower(username);
        return findAll().stream()
                .filter(user -> lower(user.getUsername()).equals(value))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        String value = lower(email);
        return findAll().stream()
                .filter(user -> lower(user.getEmail()).equals(value))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public List<User> findAllCustomers() {
        return findAll().stream()
                .filter(Customer.class::isInstance)
                .toList();
    }

    public List<User> findAllAdmins() {
        return findAll().stream()
                .filter(Admin.class::isInstance)
                .toList();
    }

    public List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email) {
        String usernameValue = lower(username);
        String emailValue = lower(email);
        return findAll().stream()
                .filter(user -> lower(user.getUsername()).contains(usernameValue)
                        || lower(user.getEmail()).contains(emailValue))
                .toList();
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
