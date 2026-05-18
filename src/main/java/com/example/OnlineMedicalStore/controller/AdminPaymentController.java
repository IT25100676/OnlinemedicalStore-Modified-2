package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.enums.PaymentStatus;
import com.example.OnlineMedicalStore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes ra) {
        try {
            paymentService.updateStatus(id, PaymentStatus.valueOf(status));
            ra.addFlashAttribute("success", "Payment status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/payment-history";
    }

    @PostMapping("/{id}/delete")
    public String deletePayment(@PathVariable Long id, RedirectAttributes ra) {
        try {
            paymentService.delete(id);
            ra.addFlashAttribute("success", "Payment deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/payment-history";
    }
}
