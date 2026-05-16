package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.Medicine;
import com.example.OnlineMedicalStore.entity.Order;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.List;

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
        List<Medicine> medicines = new ArrayList<>();
        List<Medicine> allMedicines = medicineService.findAll();
        for (int i = 0; i < allMedicines.size() && i < 8; i++) {
            medicines.add(allMedicines.get(i));
        }
        model.addAttribute("medicines", medicines);
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
        List<Order> recentOrders = new ArrayList<>();
        List<Order> allOrders = orderService.findAll();
        for (int i = 0; i < allOrders.size() && i < 5; i++) {
            recentOrders.add(allOrders.get(i));
        }
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("pendingReviews", reviewService.findPendingModeration().size());
        model.addAttribute("totalReviews", reviewService.findAll().size());
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "admin/dashboard";
    }
}
