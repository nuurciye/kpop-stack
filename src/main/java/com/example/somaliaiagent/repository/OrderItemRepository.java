package com.example.somaliaiagent.repository;

import com.example.somaliaiagent.model.MenuItem;
import com.example.somaliaiagent.model.Order;
import com.example.somaliaiagent.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByMenuItem(MenuItem menuItem);

    // You might not need many custom queries here as OrderItems are often
    // accessed via the Order entity. However, some specific ones could be:

    // Find all order items for a particular menu item across all orders
    // (e.g., to see how many times a specific dish has been ordered)
    // List<OrderItem> findByMenuItemId(Long menuItemId);
}
