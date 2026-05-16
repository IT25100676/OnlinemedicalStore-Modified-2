package com.medistore.controller;

import com.medistore.entity.User;
import com.medistore.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MedicineService medicineService;
    private final OrderService orderService;
    private final UserService userService;
    private final AdminService adminService;
    private final PrescriptionService prescriptionService;
    private final ReviewService reviewService;

    @GetMapping("/")
    public String home(Model model, HttpSession session,
            @RequestParam(required = false) String error) {
        model.addAttribute("medicines", medicineService.findAll().stream().limit(8).toList());
        model.addAttribute("categories", medicineService.getAllCategories());
        model.addAttribute("error", error);
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("currentUser", user);
        return "index";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {
        model.addAttribute("totalMedicines", medicineService.countAll());
        model.addAttribute("totalOrders", orderService.countAll());
        model.addAttribute("totalUsers", userService.countAll());
        model.addAttribute("totalAdmins", adminService.countAll());
        model.addAttribute("pendingPrescriptions", prescriptionService.countPending());
        model.addAttribute("lowStockMedicines", medicineService.findLowStock());
        model.addAttribute("expiringSoon", medicineService.findExpiringSoon());
        model.addAttribute("recentOrders", orderService.findAll().stream().limit(5).toList());
        model.addAttribute("pendingReviews", reviewService.findPendingModeration().size());
        model.addAttribute("totalReviews", reviewService.findAll().size());
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "admin/dashboard";
    }
}
