package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Payment;
import com.example.OnlineMedicalStore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByOrderByPaymentDateDesc();
}
