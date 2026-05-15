package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Order;
import com.example.OnlineMedicalStore.entity.Customer;
import com.example.OnlineMedicalStore.entity.enums.OrderStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Repository
public class OrderRepository extends AbstractTextFileRepository<Order> {

    public OrderRepository() {
        super("orders.txt");
    }

    @Override
    public Order save(Order order) {
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        return super.save(order);
    }

    public List<Order> findByCustomerOrderByOrderDateDesc(Customer customer) {
        Long customerId = customer != null ? customer.getId() : null;
        String customerEmail = lower(customer != null ? customer.getEmail() : null);
        String customerUsername = lower(customer != null ? customer.getUsername() : null);
        return findAllByOrderByOrderDateDesc().stream()
                .filter(order -> belongsToCustomer(order, customerId, customerEmail, customerUsername))
                .toList();
    }

    private boolean belongsToCustomer(Order order, Long customerId, String customerEmail, String customerUsername) {
        Customer orderCustomer = order.getCustomer();
        if (orderCustomer == null) {
            return false;
        }
        if (customerId != null && customerId.equals(orderCustomer.getId())) {
            return true;
        }
        if (!customerEmail.isBlank() && customerEmail.equals(lower(orderCustomer.getEmail()))) {
            return true;
        }
        return !customerUsername.isBlank() && customerUsername.equals(lower(orderCustomer.getUsername()));
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return findAll().stream()
                .filter(order -> order.getStatus() == status)
                .toList();
    }

    public List<Order> findAllByOrderByOrderDateDesc() {
        return findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public long countByStatus(OrderStatus status) {
        return findByStatus(status).size();
    }
}
