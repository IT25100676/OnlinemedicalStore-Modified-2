package com.medistore.service;

import com.medistore.entity.Admin;
import com.medistore.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public Admin login(String username, String password) {
        Optional<Admin> opt = adminRepository.findByUsername(username);
        if (opt.isEmpty()) {
            opt = adminRepository.findByEmail(username);
        }
        if (opt.isPresent()) {
            Admin admin = opt.get();
            if (admin.isActive() && admin.validateLogin(password)) {
                return admin;
            }
        }
        return null;
    }

    @Transactional
    public Admin registerAdmin(String username, String email, String password, String department) {
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("Admin username already taken.");
        }
        if (adminRepository.existsByEmail(email)) {
            throw new RuntimeException("Admin email already registered.");
        }
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(password);
        admin.setDepartment(department);
        return adminRepository.save(admin);
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public List<Admin> searchAdmins(String query) {
        return adminRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    public long countAll() {
        return adminRepository.count();
    }
}
