package com.example.OnlineMedicalStore.service;

import com.example.OnlineMedicalStore.entity.*;
import com.example.OnlineMedicalStore.entity.enums.PaymentStatus;
import com.example.OnlineMedicalStore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Optional<Payment> findByOrderId(Long orderId) { return paymentRepository.findByOrderId(orderId); }

    public Optional<Payment> findById(Long id) { return paymentRepository.findById(id); }

    public List<Payment> findAll() { return paymentRepository.findAllByOrderByPaymentDateDesc(); }

    @Transactional
    public Payment processCardPayment(Order order, String cardHolder, String cardLastFour,
                                      String cardType, int expiryMonth, int expiryYear) {
        CardPayment p = new CardPayment();
        p.setOrder(order);
        p.setAmount(order.getTotalAmount());
        p.setCardHolderName(cardHolder);
        p.setCardLastFour(cardLastFour);
        p.setCardType(cardType);
        p.setExpiryMonth(expiryMonth);
        p.setExpiryYear(expiryYear);
        p.processPayment();
        return paymentRepository.save(p);
    }

    @Transactional
    public Payment processCodPayment(Order order) {
        CashOnDelivery p = new CashOnDelivery();
        p.setOrder(order);
        p.setAmount(order.getTotalAmount());
        p.processPayment();
        return paymentRepository.save(p);
    }

    @Transactional
    public void updateStatus(Long paymentId, PaymentStatus status) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        p.setStatus(status);
        paymentRepository.save(p);
    }

    @Transactional
    public void delete(Long id) { paymentRepository.deleteById(id); }
}
