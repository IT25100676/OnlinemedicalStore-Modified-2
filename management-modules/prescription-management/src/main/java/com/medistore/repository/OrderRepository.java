package com.medistore.repository;

import com.medistore.entity.Order;
import com.medistore.entity.Customer;
import com.medistore.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findAllByOrderByOrderDateDesc();

    long countByStatus(OrderStatus status);
}
