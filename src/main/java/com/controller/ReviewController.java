package com.example.OnlineMedicalStore.controller

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.service.MedicineService;
import com.example.OnlineMedicalStore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
  
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final MedicineService medicineService;

    @GetMapping("/medicine/{medicineId}")
    public String reviewsForMedicine(@PathVariable Long medicineId, HttpSession session, Model model) {
        Optional<Medicine> medicineResult = medicineService.findById(medicineId);
        if (medicineResult.isEmpty()) {
            throw new RuntimeException("Medicine not found");
        }
        Medicine medicine = medicineResult.get();
        model.addAttribute("medicine", medicine);
        model.addAttribute("reviews", reviewService.findByMedicine(medicine));
        model.addAttribute("avgRating", reviewService.getAverageRating(medicine));
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "review/list";
    }

    @GetMapping("/add")
    public String addReviewPage(@RequestParam Long medicineId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        Optional<Medicine> medicineResult = medicineService.findById(medicineId);
        if (medicineResult.isEmpty()) {
            throw new RuntimeException("Medicine not found");
        }
        Medicine medicine = medicineResult.get();
        Review existingReview = null;
        if (user != null) {
            List<Review> userReviews = reviewService.findByUserAndMedicine(user, medicine);
            if (!userReviews.isEmpty()) {
                existingReview = userReviews.get(0);
            }
        }
        model.addAttribute("medicine", medicine);
        model.addAttribute("existingReview", existingReview);
        model.addAttribute("currentUser", user);
        return "review/submit";
    }

    @PostMapping("/add")
    public String addReview(@RequestParam Long medicineId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
      
        try {
            if (user == null) {
                ra.addFlashAttribute("error", "Please login before submitting a review.");
                return "redirect:/auth/customer/login";
            }
            if (comment == null || comment.isBlank()) {
                ra.addFlashAttribute("error", "Review comment is required.");
                return "redirect:/reviews/add?medicineId=" + medicineId;
            }
            Optional<Medicine> medicineResult = medicineService.findById(medicineId);
            if (medicineResult.isEmpty()) {
                throw new RuntimeException("Medicine not found");
            }
            Medicine medicine = medicineResult.get();
            Review existingReview = null;
            List<Review> userReviews = reviewService.findByUserAndMedicine(user, medicine);
            if (!userReviews.isEmpty()) {
                existingReview = userReviews.get(0);
            }
            if (existingReview != null) {
                reviewService.updateByUser(existingReview.getId(), user, rating, comment);
                ra.addFlashAttribute("success", "Review updated. The moderation queue now has your latest version.");
            } else {
                reviewService.addReview(user, medicineId, rating, comment, false, null);
                ra.addFlashAttribute("success", "Review submitted. Pending moderation.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reviews/add?medicineId=" + medicineId;
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        Optional<Review> reviewResult = reviewService.findById(id);
        if (reviewResult.isEmpty()) {
            throw new RuntimeException("Review not found");
        }
        Review review = reviewResult.get();
        if (user == null) {
            return "redirect:/auth/customer/login";
        }
        if (review.getUser() == null || !user.getId().equals(review.getUser().getId())) {
            return "redirect:/medicines/" + review.getMedicine().getId();
        }
        return "redirect:/reviews/add?medicineId=" + review.getMedicine().getId();
    }

  
    @PostMapping("/{id}/edit")
    public String editReview(@PathVariable Long id, @RequestParam int rating,
                             @RequestParam String comment, HttpSession session, RedirectAttributes ra) {
        try {
            User user = (User) session.getAttribute("loggedInUser");
            Review r = reviewService.updateByUser(id, user, rating, comment);
            ra.addFlashAttribute("success", "Review updated. The moderation queue now has your latest version.");
            return "redirect:/reviews/add?medicineId=" + r.getMedicine().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            if (reviewService.findById(id).isPresent()) {
                Review review = reviewService.findById(id).get();
                return "redirect:/reviews/add?medicineId=" + review.getMedicine().getId();
            }
            return "redirect:/";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Optional<Review> reviewResult = reviewService.findById(id);
            if (reviewResult.isEmpty()) {
                throw new RuntimeException("Review not found");
            }
            Review r = reviewResult.get();
            Long medId = r.getMedicine().getId();
            reviewService.delete(id);
            ra.addFlashAttribute("success", "Review deleted.");
            return "redirect:/medicines/" + medId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }
  

    @GetMapping("/admin/moderate")
    public String legacyModerationPanel() {
        return "redirect:/reviews/admin/reviews";
    }

    @GetMapping("/admin/reviews")
    public String managementPanel(@RequestParam(defaultValue = "pending") String status,
                                  HttpSession session, Model model) {
        model.addAttribute("reviews", "all".equalsIgnoreCase(status)
                ? reviewService.findAll() : reviewService.findPendingModeration());
        model.addAttribute("status", status);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "review/moderate";
    }

    @PostMapping("/admin/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        try {
            reviewService.approve(id);
            ra.addFlashAttribute("success", "Review approved.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reviews/admin/reviews";
    }

    @PostMapping("/admin/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        try {
            reviewService.delete(id);
            ra.addFlashAttribute("success", "Review rejected and removed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reviews/admin/reviews";
    }
}
