package com.medistore.controller;

import com.medistore.entity.*;
import com.medistore.entity.enums.OrderStatus;
import com.medistore.service.MedicineService;
import com.medistore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MedicineService medicineService;

    /* ===== CART ===== */
    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) { cart = new HashMap<>(); session.setAttribute("cart", cart); }
        return cart;
    }

    private Map<Medicine, Integer> buildCartItems(Map<Long, Integer> cart) {
        Map<Medicine, Integer> cartItems = new HashMap<>();
        for (Map.Entry<Long, Integer> e : cart.entrySet()) {
            medicineService.findById(e.getKey()).ifPresent(m -> cartItems.put(m, e.getValue()));
        }
        return cartItems;
    }

    private BigDecimal calculateCartTotal(Map<Medicine, Integer> cartItems) {
        return cartItems.entrySet().stream()
                .map(e -> e.getKey().getPrice().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateCartItemCount(Map<Medicine, Integer> cartItems) {
        return cartItems.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
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
        cart.merge(medicineId, quantity, Integer::sum);
        ra.addFlashAttribute("success", "Added to cart!");
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        Map<Long, Integer> cart = getCart(session);
        Map<Medicine, Integer> cartItems = buildCartItems(cart);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", calculateCartTotal(cartItems));
        model.addAttribute("cartItemCount", calculateCartItemCount(cartItems));
        model.addAttribute("currentUser", session.getAttribute("loggedInUser"));
        return "order/cart";
    }

    @PostMapping("/cart/update/{medicineId}")
    public String updateCart(@PathVariable("medicineId") Long medicineId,
                             @RequestParam("quantity") int quantity,
                             HttpSession session,
                             RedirectAttributes ra) {
        Map<Long, Integer> cart = getCart(session);
        if (quantity <= 0) cart.remove(medicineId);
        else {
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

    /* ===== CHECKOUT ===== */
    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        Map<Long, Integer> cart = getCart(session);
        if (cart.isEmpty()) return "redirect:/cart";
        Map<Medicine, Integer> cartItems = buildCartItems(cart);
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", calculateCartTotal(cartItems));
        model.addAttribute("cartItemCount", calculateCartItemCount(cartItems));
        model.addAttribute("currentUser", user);
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam("deliveryAddress") String deliveryAddress,
                             @RequestParam(name = "notes", required = false) String notes,
                             HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (!(user instanceof Customer customer)) {
            ra.addFlashAttribute("error", "Only customers can place orders.");
            return "redirect:/cart";
        }
        try {
            Map<Long, Integer> cart = getCart(session);
            Order order = orderService.placeOrder(customer, cart, deliveryAddress, notes);
            session.removeAttribute("cart");
            return "redirect:/orders/" + order.getId() + "/payment";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /* ===== ORDERS ===== */
    @GetMapping("/orders")
    public String orderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("orders", (user instanceof Customer c)
                ? orderService.findByCustomer(c)
                : orderService.findAll());
        model.addAttribute("currentUser", user);
        return "order/history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Long id, HttpSession session, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
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
