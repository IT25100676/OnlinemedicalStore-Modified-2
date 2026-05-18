package com.example.OnlineMedicalStore.controller;

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
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AdminService adminService;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String newPassword,
                                HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        try {
            User updated = userService.updateUser(user.getId(), email, phone, address,
                    (newPassword != null && !newPassword.isBlank()) ? newPassword : null);
            session.setAttribute("loggedInUser", updated);
            ra.addFlashAttribute("success", "Profile updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/admin/users")
    public String userList(@RequestParam(required = false) String search, Model model, HttpSession session) {
        model.addAttribute("users", search != null && !search.isBlank()
                ? userService.searchUsers(search) : userService.findAllCustomers());
        model.addAttribute("admins", search != null && !search.isBlank()
                ? adminService.searchAdmins(search) : adminService.findAll());
        model.addAttribute("search", search);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "user/list";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("success", "User deactivated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String address,
                             @RequestParam(required = false) String password,
                             RedirectAttributes ra) {
        try {
            userService.updateUser(id, email, phone, address,
                    password != null && !password.isBlank() ? password : null);
            ra.addFlashAttribute("success", "User updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.activateUser(id);
            ra.addFlashAttribute("success", "User activated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/create-admin")
    public String createAdmin(@RequestParam String username, @RequestParam String email,
                              @RequestParam String password, @RequestParam(required = false) String department,
                              RedirectAttributes ra) {
        try {
            adminService.registerAdmin(username, email, password, department);
            ra.addFlashAttribute("success", "Admin created successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
