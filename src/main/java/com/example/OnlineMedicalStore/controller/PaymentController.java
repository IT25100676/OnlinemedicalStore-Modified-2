package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.service.OrderService;
import com.example.OnlineMedicalStore.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @GetMapping("/{orderId}/payment")
    public String paymentPage(@PathVariable Long orderId, HttpSession session, Model model) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("order", order);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "payment/page";
    }

    @PostMapping("/{orderId}/payment")
    public String processPayment(@PathVariable Long orderId,
                                 @RequestParam String paymentType,
                                 @RequestParam(required = false) String cardHolder,
                                 @RequestParam(required = false) String cardNumber,
                                 @RequestParam(required = false) String cardType,
                                 @RequestParam(required = false) Integer expiryMonth,
                                 @RequestParam(required = false) Integer expiryYear,
                                 RedirectAttributes ra) {
        try {
            Order order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            Payment payment;
            if ("CARD".equalsIgnoreCase(paymentType)) {
                String lastFour = (cardNumber != null && cardNumber.length() >= 4)
                        ? cardNumber.substring(cardNumber.length() - 4) : "0000";
                payment = paymentService.processCardPayment(order, cardHolder, lastFour,
                        cardType, expiryMonth != null ? expiryMonth : 1,
                        expiryYear != null ? expiryYear : 2025);
            } else {
                payment = paymentService.processCodPayment(order);
            }
            return "redirect:/orders/" + orderId + "/invoice";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/" + orderId + "/payment";
        }
    }

    @GetMapping("/{orderId}/invoice")
    public String invoice(@PathVariable Long orderId, HttpSession session, Model model) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Payment payment = paymentService.findByOrderId(orderId).orElse(null);
        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "payment/invoice";
    }

    @GetMapping("/payment-history")
    public String paymentHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/auth/customer/login";
        }
        
        List<Payment> allPayments = paymentService.findAll();
        List<Payment> userPayments;
        
        if ("ADMIN".equals(user.getRoleString())) {
            userPayments = allPayments;
        } else {
            userPayments = allPayments.stream()
                .filter(p -> p.getOrder().getCustomer().getId().equals(user.getId()))
                .collect(Collectors.toList());
        }
        
        model.addAttribute("payments", userPayments);
        model.addAttribute("currentUser", user);
        return "payment/history";
    }
}
