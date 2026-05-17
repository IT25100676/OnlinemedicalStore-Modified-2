package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.Medicine;
import com.example.OnlineMedicalStore.entity.Review;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.service.MedicineService;
import com.example.OnlineMedicalStore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicineController {
    

    private final MedicineService medicineService;
    private final ReviewService reviewService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String category,
                       Model model, HttpSession session) {
        model.addAttribute("medicines", (search != null && !search.isBlank())
                ? medicineService.search(search)
                : (category != null && !category.isBlank())
                ? medicineService.findByCategory(category)
                : medicineService.findAll());
        model.addAttribute("categories", medicineService.getAllCategories());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "medicine/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Medicine medicine = medicineService.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        User user = (User) session.getAttribute("loggedInUser");
        Review existingReview = user != null
                ? reviewService.findByUserAndMedicine(user, medicine).stream().findFirst().orElse(null)
                : null;
        model.addAttribute("medicine", medicine);
        model.addAttribute("reviews", reviewService.findByMedicine(medicine));
        model.addAttribute("existingReview", existingReview);
        model.addAttribute("avgRating", reviewService.getAverageRating(medicine));
        model.addAttribute("currentUser", user);
        return "medicine/detail";
    }

    @GetMapping("/add")
    public String addPage(Model model, HttpSession session) {
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "medicine/add";
    }

    @PostMapping("/add")
    public String add(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        try {
            medicineService.addMedicine(params.get("medicineType"), params);
            ra.addFlashAttribute("success", "Medicine added successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/medicines";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("medicine", medicineService.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found")));
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "medicine/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @RequestParam Map<String, String> params, RedirectAttributes ra) {
        try {
            medicineService.updateMedicine(id, params);
            ra.addFlashAttribute("success", "Medicine updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/medicines";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            medicineService.deleteMedicine(id);
            ra.addFlashAttribute("success", "Medicine deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/medicines";
    }
}
