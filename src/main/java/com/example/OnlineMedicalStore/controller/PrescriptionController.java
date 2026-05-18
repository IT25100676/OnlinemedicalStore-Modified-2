package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.Admin;
import com.example.OnlineMedicalStore.entity.Prescription;
import com.example.OnlineMedicalStore.entity.User;
import com.example.OnlineMedicalStore.entity.enums.PrescriptionStatus;
import com.example.OnlineMedicalStore.service.PrescriptionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Web controller for Prescription Management (Component 04).
 *
 * Handles all HTTP requests and routes them to PrescriptionService.
 * Demonstrates Encapsulation — the controller never accesses the
 * repository directly; all logic is delegated to the service layer.
 *
 * Endpoints:
 *   GET  /prescriptions              → list (customer: own | admin: all)
 *   GET  /prescriptions/upload       → upload form
 *   POST /prescriptions/upload       → CREATE — save new prescription
 *   GET  /prescriptions/admin/review → admin review queue (PENDING only)
 *   GET  /prescriptions/search       → READ with search/filter
 *   GET  /prescriptions/{id}/review  → review form for admin
 *   GET  /prescriptions/{id}/file    → READ — stream file to browser
 *   POST /prescriptions/{id}/review  → UPDATE — approve / reject
 *   POST /prescriptions/{id}/delete  → DELETE — remove prescription
 */
@Controller
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // ── Helper: get current user from session ─────────────────────────────────

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRoleString());
    }

    // ── READ: List ────────────────────────────────────────────────────────────

    /**
     * List all prescriptions.
     * - Customer  → sees only their own prescriptions
     * - Admin     → sees all prescriptions from all customers
     */
    @GetMapping
    public String list(HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("prescriptions",
                isAdmin(user)
                        ? prescriptionService.findAll()
                        : prescriptionService.findByUser(user));
        model.addAttribute("currentUser", user);
        model.addAttribute("pendingCount", prescriptionService.countPending());
        return "prescription/list";
    }

    // ── READ: Admin review queue ──────────────────────────────────────────────

    /**
     * Admin-only view showing only PENDING prescriptions awaiting review.
     */
    @GetMapping("/admin/review")
    public String adminReviewList(HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("prescriptions", prescriptionService.findPending());
        model.addAttribute("currentUser", user);
        model.addAttribute("adminRxPage", true);
        model.addAttribute("pendingCount", prescriptionService.countPending());
        return "prescription/list";
    }

    // ── READ: Search ──────────────────────────────────────────────────────────

    /**
     * Admin search across all prescriptions by doctor name or customer username.
     * Demonstrates the READ (search/filter) CRUD operation.
     */
    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false) String keyword,
                         HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("prescriptions", prescriptionService.search(keyword));
        model.addAttribute("currentUser", user);
        model.addAttribute("searchQuery", keyword);
        model.addAttribute("pendingCount", prescriptionService.countPending());
        return "prescription/list";
    }

    // ── CREATE: Upload form ───────────────────────────────────────────────────

    @GetMapping("/upload")
    public String uploadPage(@RequestParam(name = "returnTo", required = false) String returnTo,
                             HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("currentUser", user);
        model.addAttribute("returnTo", sanitizeReturnTo(returnTo));
        return "prescription/upload";
    }

    // ── CREATE: Handle upload POST ────────────────────────────────────────────

    /**
     * Receives the uploaded file and metadata, delegates to PrescriptionService.
     * On success, redirects back to the page the user came from (e.g. checkout).
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(name = "doctorName", required = false) String doctorName,
                         @RequestParam(name = "issueDate", required = false) String issueDate,
                         @RequestParam(name = "returnTo", required = false) String returnTo,
                         HttpSession session, RedirectAttributes ra) {

        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        try {
            LocalDate date = (issueDate != null && !issueDate.isBlank())
                    ? LocalDate.parse(issueDate) : null;
            prescriptionService.upload(user, file, doctorName, date);
            ra.addFlashAttribute("success",
                    "Prescription uploaded successfully! It is now pending admin review.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/prescriptions/upload";
        }

        return "redirect:" + sanitizeReturnTo(returnTo);
    }

    // ── READ: Stream file to browser ─────────────────────────────────────────

    /**
     * Returns the raw file bytes so the browser can display/download the prescription.
     * Access control: admin can view any file; customer can only view their own.
     */
    @GetMapping("/{id:\\d+}/file")
    public ResponseEntity<byte[]> file(@PathVariable("id") Long id, HttpSession session) {
        User user = currentUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Prescription p = prescriptionService.findById(id).orElse(null);
        if (p == null || p.getFileContent() == null)
            return ResponseEntity.notFound().build();

        boolean admin = isAdmin(user);
        boolean owner = p.getUser() != null
                && Objects.equals(p.getUser().getId(), user.getId());
        if (!admin && !owner)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String fileName = (p.getFileName() == null || p.getFileName().isBlank())
                ? "prescription" : p.getFileName();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (p.getContentType() != null && !p.getContentType().isBlank()) {
            try { mediaType = MediaType.parseMediaType(p.getContentType()); }
            catch (Exception ignored) { }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString())
                .body(p.getFileContent());
    }

    // ── UPDATE: Review form (GET) ─────────────────────────────────────────────

    @GetMapping("/{id:\\d+}/review")
    public String reviewPage(@PathVariable("id") Long id,
                             HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        Prescription p = prescriptionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        model.addAttribute("prescription", p);
        model.addAttribute("currentUser", user);
        return "prescription/review";
    }

    // ── UPDATE: Submit review decision (POST) ─────────────────────────────────

    /**
     * Admin submits APPROVED / REJECTED / PENDING decision.
     * Polymorphic: PrescriptionService.applyValidationRules() applies
     * different rules based on the chosen status.
     */
    @PostMapping("/{id:\\d+}/review")
    public String review(@PathVariable("id") Long id,
                         @RequestParam("status") String status,
                         @RequestParam(name = "notes", required = false) String notes,
                         HttpSession session, RedirectAttributes ra) {

        User user = currentUser(session);
        if (user == null) return "redirect:/login";
        if (!isAdmin(user)) {
            ra.addFlashAttribute("error", "Access denied.");
            return "redirect:/prescriptions";
        }

        Admin admin = (Admin) session.getAttribute("loggedInUser");
        try {
            prescriptionService.review(id, PrescriptionStatus.valueOf(status), notes, admin);
            ra.addFlashAttribute("success", "Prescription #" + id + " has been reviewed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/prescriptions/" + id + "/review";
        }
        return "redirect:/prescriptions/admin/review";
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Delete a prescription.
     * Customers can only delete their own PENDING prescriptions.
     * Admins can delete any prescription.
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id,
                         HttpSession session, RedirectAttributes ra) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        try {
            prescriptionService.delete(id, user, isAdmin(user));
            ra.addFlashAttribute("success", "Prescription deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/prescriptions";
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Prevent open-redirect attacks by only allowing relative paths starting with / */
    private String sanitizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank()) return "/prescriptions";
        return returnTo.startsWith("/") && !returnTo.startsWith("//")
                ? returnTo : "/prescriptions";
    }
}
