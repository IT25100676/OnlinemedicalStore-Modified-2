package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.entity.enums.PrescriptionStatus;
import com.example.OnlineMedicalStore.service.PrescriptionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("prescriptions", "ADMIN".equals(user.getRoleString())
                ? prescriptionService.findAll() : prescriptionService.findByUser(user));
        model.addAttribute("currentUser", user);
        return "prescription/list";
    }

    @GetMapping("/upload")
    public String uploadPage(HttpSession session, Model model) {
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "prescription/upload";
    }

    @GetMapping("/admin/review")
    public String adminReviewList(HttpSession session, Model model) {
        model.addAttribute("prescriptions", prescriptionService.findPending());
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        model.addAttribute("adminRxPage", true);
        return "prescription/list";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(name = "doctorName", required = false) String doctorName,
                         @RequestParam(name = "issueDate", required = false) String issueDate,
                         HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        try {
            if (file.isEmpty()) throw new RuntimeException("Please select a file.");
            LocalDate date = (issueDate != null && !issueDate.isBlank()) ? LocalDate.parse(issueDate) : null;
            prescriptionService.upload(user, file, doctorName, date);
            ra.addFlashAttribute("success", "Prescription uploaded successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prescriptions";
    }

    @GetMapping("/{id:\\d+}/review")
    public String reviewPage(@PathVariable("id") Long id, HttpSession session, Model model) {
        model.addAttribute("prescription", prescriptionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found")));
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "prescription/review";
    }

    @PostMapping("/{id:\\d+}/review")
    public String review(@PathVariable("id") Long id, @RequestParam("status") String status,
                         @RequestParam(name = "notes", required = false) String notes,
                         HttpSession session, RedirectAttributes ra) {
        Admin admin = (Admin) session.getAttribute("loggedInUser");
        try {
            prescriptionService.review(id, PrescriptionStatus.valueOf(status), notes, admin);
            ra.addFlashAttribute("success", "Prescription reviewed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prescriptions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            prescriptionService.delete(id);
            ra.addFlashAttribute("success", "Prescription deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prescriptions";
    }
}
