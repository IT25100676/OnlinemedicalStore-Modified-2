package com.example.OnlineMedicalStore.service;

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.entity.enums.OrderStatus;
import com.example.OnlineMedicalStore.repository.MedicineRepository;
import com.example.OnlineMedicalStore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;

    public List<Order> findByCustomer(Customer customer) {
        return orderRepository.findByCustomerOrderByOrderDateDesc(customer);
    }

    public Optional<Order> findById(Long id) { return orderRepository.findById(id); }

    public List<Order> findAll() { return orderRepository.findAllByOrderByOrderDateDesc(); }

    public long countByStatus(OrderStatus status) { return orderRepository.countByStatus(status); }

    public long countAll() { return orderRepository.count(); }

    @Transactional
    public Order placeOrder(Customer customer, Map<Long, Integer> cart, String deliveryAddress, String notes) {
        if (cart == null || cart.isEmpty()) throw new RuntimeException("Cart is empty.");
        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryAddress(deliveryAddress);
        order.setNotes(notes);

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Medicine medicine = medicineRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Medicine not found: " + entry.getKey()));
            if (medicine.getStock() < entry.getValue())
                throw new RuntimeException("Insufficient stock for: " + medicine.getName());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMedicine(medicine);
            item.setQuantity(entry.getValue());
            item.setUnitPrice(medicine.getPrice());
            order.getItems().add(item);

            medicine.setStock(medicine.getStock() - entry.getValue());
            medicineRepository.save(medicine);
        }
        order.calculateTotal();
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateDelivery(Long orderId, String deliveryAddress, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (deliveryAddress != null && !deliveryAddress.isBlank()) {
            order.setDeliveryAddress(deliveryAddress);
        }
        order.setNotes(notes);
        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.DELIVERED)
            throw new RuntimeException("Cannot cancel a delivered order.");
        // Restore stock
        for (OrderItem item : order.getItems()) {
            Medicine med = item.getMedicine();
            med.setStock(med.getStock() + item.getQuantity());
            medicineRepository.save(med);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
}
