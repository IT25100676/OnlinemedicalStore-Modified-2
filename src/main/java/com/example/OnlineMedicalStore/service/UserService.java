package com.example.OnlineMedicalStore.service;

import com.example.OnlineMedicalStore.entity.Admin;
import com.example.OnlineMedicalStore.entity.Customer;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User login(String username, String password) {
        if (username == null || username.isBlank()) return null;
        String login = username.trim();
        Optional<User> opt = userRepository.findByUsernameIgnoreCase(login);
        if (opt.isEmpty()) {
            opt = userRepository.findByEmailIgnoreCase(login);
        }
        if (opt.isPresent()) {
            User user = opt.get();
            if (user.isActive() && user.validateLogin(password)) return user;
        }
        return null;
    }

    @Transactional
    public Customer registerCustomer(String username, String email, String password, String phone, String address) {
        if (userRepository.existsByUsername(username)) throw new RuntimeException("Username already taken.");
        if (userRepository.existsByEmail(email)) throw new RuntimeException("Email already registered.");
        Customer c = new Customer();
        c.setUsername(username);
        c.setEmail(email);
        c.setPassword(password);
        c.setPhone(phone);
        c.setAddress(address);
        return userRepository.save(c);
    }

    @Transactional
    public Admin registerAdmin(String username, String email, String password, String department) {
        if (userRepository.existsByUsername(username)) throw new RuntimeException("Username already taken.");
        Admin a = new Admin();
        a.setUsername(username);
        a.setEmail(email);
        a.setPassword(password);
        a.setDepartment(department);
        return userRepository.save(a);
    }

    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    public List<User> findAll() { return userRepository.findAll(); }

    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    @Transactional
    public User updateUser(Long id, String email, String phone, String address, String password) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (email != null && !email.isBlank()) user.setEmail(email);
        if (phone != null && !phone.isBlank()) user.setPhone(phone);
        if (address != null && !address.isBlank()) user.setAddress(address);
        if (password != null && !password.isBlank()) user.setPassword(password);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    public long countAll() { return userRepository.count(); }

    public List<User> findAllCustomers() { return userRepository.findAllCustomers(); }
}
