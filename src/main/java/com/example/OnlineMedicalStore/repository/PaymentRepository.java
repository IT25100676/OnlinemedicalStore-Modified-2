package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Payment;
import com.example.OnlineMedicalStore.entity.Order;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository extends AbstractTextFileRepository<Payment> {

    public PaymentRepository() {
        super("payments.txt");
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        if (payment.getTransactionId() == null) {
            payment.setTransactionId("TXN" + System.currentTimeMillis());
        }
        return super.save(payment);
    }

    public Optional<Payment> findByOrder(Order order) {
        Long orderId = order != null ? order.getId() : null;
        return findByOrderId(orderId);
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        return findAll().stream()
                .filter(payment -> payment.getOrder() != null && orderId != null
                        && orderId.equals(payment.getOrder().getId()))
                .findFirst();
    }

    public List<Payment> findAllByOrderByPaymentDateDesc() {
        return findAll().stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
