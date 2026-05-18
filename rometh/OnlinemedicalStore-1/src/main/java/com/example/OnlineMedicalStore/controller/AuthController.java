package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.Admin;
import com.example.OnlineMedicalStore.entity.Customer;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.service.AdminService;
import com.example.OnlineMedicalStore.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AdminService adminService;

    @GetMapping("/customer/login")
    public String customerLoginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "auth/customer-login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "auth/login";
    }

    @PostMapping("/customer/login")
    public String customerLogin(@RequestParam String username, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        User user = userService.login(username, password);
        if (user == null || !"CUSTOMER".equals(user.getRoleString())) {
            ra.addFlashAttribute("error", "Invalid customer username or password.");
            return "redirect:/auth/customer/login";
        }
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRoleString());
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        User user = userService.login(username, password);
        if (user == null) {
            ra.addFlashAttribute("error", "Invalid username or password.");
            return "redirect:/auth/login";
        }
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRoleString());
        return "ADMIN".equals(user.getRoleString()) ? "redirect:/admin/dashboard" : "redirect:/";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "auth/admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        Admin admin = adminService.login(username, password);
        if (admin == null) {
            ra.addFlashAttribute("error", "Invalid admin username or password.");
            return "redirect:/auth/admin/login";
        }
        session.setAttribute("loggedInUser", admin);
        session.setAttribute("userId", admin.getId());
        session.setAttribute("userRole", admin.getRoleString());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() { return "auth/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email,
                           @RequestParam String password, @RequestParam String confirmPassword,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String address,
                           HttpSession session, RedirectAttributes ra) {
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/auth/register";
        }
        try {
            Customer c = userService.registerCustomer(username, email, password, phone, address);
            session.setAttribute("loggedInUser", c);
            session.setAttribute("userId", c.getId());
            session.setAttribute("userRole", "CUSTOMER");
            return "redirect:/";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
