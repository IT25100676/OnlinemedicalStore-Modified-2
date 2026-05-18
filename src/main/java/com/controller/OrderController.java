package com.example.OnlineMedicalStore.controller;

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.entity.enums.OrderStatus;
import com.example.OnlineMedicalStore.service.MedicineService;
import com.example.OnlineMedicalStore.service.OrderService;
import com.example.OnlineMedicalStore.service.PrescriptionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MedicineService medicineService;
    private final PrescriptionService prescriptionService;

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    private Map<Medicine, Integer> buildCartItems(Map<Long, Integer> cart) {
        Map<Medicine, Integer> cartItems = new HashMap<>();
        for (Map.Entry<Long, Integer> e : cart.entrySet()) {
            Optional<Medicine> medicine = medicineService.findById(e.getKey());
            if (medicine.isPresent()) {
                cartItems.put(medicine.get(), e.getValue());
            }
        }
        return cartItems;
    }

    private BigDecimal calculateCartTotal(Map<Medicine, Integer> cartItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Medicine, Integer> item : cartItems.entrySet()) {
            BigDecimal lineTotal = item.getKey().getPrice().multiply(BigDecimal.valueOf(item.getValue()));
            total = total.add(lineTotal);
        }
        return total;
    }

    private int calculateCartItemCount(Map<Medicine, Integer> cartItems) {
        int count = 0;
        for (Integer quantity : cartItems.values()) {
            count = count + quantity;
        }
        return count;
    }

    private boolean containsPrescriptionMedicine(Map<Medicine, Integer> cartItems) {
        for (Medicine medicine : cartItems.keySet()) {
            if (medicine.isRequiresPrescription()) {
                return true;
            }
        }
        return false;
    }

    private void addCartAttributes(Model model, Map<Medicine, Integer> cartItems, User user) {
        boolean prescriptionRequired = containsPrescriptionMedicine(cartItems);
        boolean hasApprovedPrescription = prescriptionService.hasApprovedPrescription(user);
        boolean hasPendingPrescription = prescriptionService.hasPendingPrescription(user);
        boolean hasSubmittedPrescription = hasApprovedPrescription || hasPendingPrescription;
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", calculateCartTotal(cartItems));
        model.addAttribute("cartItemCount", calculateCartItemCount(cartItems));
        model.addAttribute("currentUser", user);
        model.addAttribute("prescriptionRequired", prescriptionRequired);
        model.addAttribute("hasApprovedPrescription", hasApprovedPrescription);
        model.addAttribute("hasPendingPrescription", hasPendingPrescription);
        model.addAttribute("hasSubmittedPrescription", hasSubmittedPrescription);
        model.addAttribute("canCheckout", !prescriptionRequired || hasSubmittedPrescription);
    }

    @PostMapping("/cart/add/{medicineId}")
    public String addToCart(@PathVariable("medicineId") Long medicineId,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            HttpSession session, RedirectAttributes ra) {
        if (quantity <= 0) {
            ra.addFlashAttribute("error", "Quantity must be at least 1.");
            return "redirect:/medicines/" + medicineId;
        }
        Medicine medicine = medicineService.findById(medicineId).orElse(null);
        if (medicine == null) {
            ra.addFlashAttribute("error", "Medicine not found.");
            return "redirect:/medicines";
        }
        Map<Long, Integer> cart = getCart(session);
        int newQuantity = cart.getOrDefault(medicineId, 0) + quantity;
        if (newQuantity > medicine.getStock()) {
            ra.addFlashAttribute("error", "Only " + medicine.getStock() + " items available for " + medicine.getName() + ".");
            return "redirect:/cart";
        }
        cart.put(medicineId, newQuantity);
        ra.addFlashAttribute("success", "Added to cart!");
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        Map<Long, Integer> cart = getCart(session);
        Map<Medicine, Integer> cartItems = buildCartItems(cart);
        User user = (User) session.getAttribute("loggedInUser");
        addCartAttributes(model, cartItems, user);
        return "order/cart";
    }

    @PostMapping("/cart/update/{medicineId}")
    public String updateCart(@PathVariable("medicineId") Long medicineId,
                             @RequestParam("quantity") int quantity,
                             HttpSession session,
                             RedirectAttributes ra) {
        Map<Long, Integer> cart = getCart(session);
        if (quantity <= 0) {
            cart.remove(medicineId);
        } else {
            Medicine medicine = medicineService.findById(medicineId).orElse(null);
            if (medicine == null) {
                cart.remove(medicineId);
                ra.addFlashAttribute("error", "Medicine not found.");
            } else if (quantity > medicine.getStock()) {
                cart.put(medicineId, medicine.getStock());
                ra.addFlashAttribute("error", "Only " + medicine.getStock() + " items available for " + medicine.getName() + ".");
            } else {
                cart.put(medicineId, quantity);
                ra.addFlashAttribute("success", "Cart updated.");
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{medicineId}")
    public String removeFromCart(@PathVariable("medicineId") Long medicineId, HttpSession session) {
        getCart(session).remove(medicineId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, RedirectAttributes ra) {
        Map<Long, Integer> cart = getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        Map<Medicine, Integer> cartItems = buildCartItems(cart);
        User user = (User) session.getAttribute("loggedInUser");
        if (containsPrescriptionMedicine(cartItems) && !prescriptionService.hasSubmittedPrescription(user)) {
            ra.addFlashAttribute("error", "Your cart contains prescription medicine. Please upload a prescription before checkout.");
            return "redirect:/prescriptions/upload?returnTo=/cart";
        }
        addCartAttributes(model, cartItems, user);
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam("deliveryAddress") String deliveryAddress,
                             @RequestParam(name = "notes", required = false) String notes,
                             HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!(user instanceof Customer)) {
            ra.addFlashAttribute("error", "Only customers can place orders.");
            return "redirect:/cart";
        }
        Customer customer = (Customer) user;
        try {
            Map<Long, Integer> cart = getCart(session);
            Map<Medicine, Integer> cartItems = buildCartItems(cart);
            if (containsPrescriptionMedicine(cartItems) && !prescriptionService.hasSubmittedPrescription(customer)) {
                ra.addFlashAttribute("error", "Please upload a prescription before checking out with prescription medicine.");
                return "redirect:/prescriptions/upload?returnTo=/cart";
            }
            Order order = orderService.placeOrder(customer, cart, deliveryAddress, notes);
            session.removeAttribute("cart");
            return "redirect:/orders/" + order.getId() + "/payment";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/orders")
    public String orderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            model.addAttribute("orders", orderService.findByCustomer(customer));
        } else {
            model.addAttribute("orders", orderService.findAll());
        }
        model.addAttribute("currentUser", user);
        return "order/history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Long id, HttpSession session, Model model) {
        Optional<Order> orderResult = orderService.findById(id);
        if (orderResult.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        Order order = orderResult.get();
        model.addAttribute("order", order);
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "order/detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            orderService.cancelOrder(id);
            ra.addFlashAttribute("success", "Order cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateStatus(@PathVariable("id") Long id, @RequestParam("status") String status, RedirectAttributes ra) {
        try {
            orderService.updateStatus(id, OrderStatus.valueOf(status));
            ra.addFlashAttribute("success", "Order status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/admin/orders/{id}/delivery")
    public String updateDelivery(@PathVariable("id") Long id,
                                 @RequestParam("deliveryAddress") String deliveryAddress,
                                 @RequestParam(name = "notes", required = false) String notes,
                                 RedirectAttributes ra) {
        try {
            orderService.updateDelivery(id, deliveryAddress, notes);
            ra.addFlashAttribute("success", "Order delivery details updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/admin/orders/{id}/delete")
    public String deleteOrder(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            orderService.deleteOrder(id);
            ra.addFlashAttribute("success", "Order deleted.");
            return "redirect:/orders";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/" + id;
        }
    }
}
