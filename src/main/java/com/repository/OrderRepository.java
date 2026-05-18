package com.example.OnlineMedicalStore.repository;

import com.example.OnlineMedicalStore.entity.Order;
import com.example.OnlineMedicalStore.entity.Customer;
import com.example.OnlineMedicalStore.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            select distinct o from Order o
            left join fetch o.customer
            left join fetch o.items i
            left join fetch i.medicine
            where o.customer = :customer
            order by o.orderDate desc
            """)
    List<Order> findByCustomerWithItemsOrderByOrderDateDesc(@Param("customer") Customer customer);

    List<Order> findByStatus(OrderStatus status);

    @Query("""
            select distinct o from Order o
            left join fetch o.customer
            left join fetch o.items i
            left join fetch i.medicine
            order by o.orderDate desc
            """)
    List<Order> findAllWithItemsOrderByOrderDateDesc();

    @Query("""
            select distinct o from Order o
            left join fetch o.customer
            left join fetch o.items i
            left join fetch i.medicine
            where o.id = :id
            """)
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    long countByStatus(OrderStatus status);
}
