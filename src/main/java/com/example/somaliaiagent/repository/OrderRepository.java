package com.example.somaliaiagent.repository;

import com.example.somaliaiagent.model.Customer;
import com.example.somaliaiagent.model.Order;
import com.example.somaliaiagent.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer(Customer customer);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderType(String orderType);

    List<Order> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Order> findByEstimatedPickupTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // Find orders for a specific customer with a specific status
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);

    // Find orders that need POS sync
    List<Order> findByPosSyncStatus(String posSyncStatus);
}
