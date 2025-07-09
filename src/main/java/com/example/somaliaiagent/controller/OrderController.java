package com.example.somaliaiagent.controller;

import com.example.somaliaiagent.model.Order;
import com.example.somaliaiagent.model.OrderItem;
import com.example.somaliaiagent.model.OrderStatus;
import com.example.somaliaiagent.service.OrderService;
// Import DTOs if you create them, e.g.:
// import com.example.somaliaiagent.dto.OrderRequestDTO;
// import com.example.somaliaiagent.dto.OrderItemRequestDTO;
// import com.example.somaliaiagent.dto.OrderStatusUpdateDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET /api/orders: Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) OrderStatus status
    ) {
        List<Order> orders;
        if (customerId != null) {
            orders = orderService.getOrdersByCustomerId(customerId);
        } else if (status != null) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }
        return ResponseEntity.ok(orders);
    }

    // GET /api/orders/{id}: Get a specific order by ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/orders: Create a new order
    // This endpoint would ideally take a DTO representing the order request
    // For simplicity in this stub, we might expect a fully formed Order object,
    // but this is not ideal for real-world scenarios.
    // A DTO would be better: e.g. @RequestBody OrderRequestDTO orderRequest
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order orderRequest) {
        // In a real app, orderRequest would be a DTO.
        // The service layer would then construct the Order entity.
        // For this stub, assuming orderRequest contains customer ID and items.
        // This is a simplified approach and needs proper DTO mapping in a full implementation.
        try {
            // Example: If orderRequest is a DTO (OrderRequestDTO orderRequestDto)
            // Order newOrder = orderService.createOrder(orderRequestDto);

            // Simplified stub: Assuming the Order object is somewhat prepared.
            // This needs significant refinement with DTOs and proper service logic.
            if (orderRequest.getCustomer() == null || orderRequest.getCustomer().getId() == null) {
                return ResponseEntity.badRequest().build(); // Or throw specific exception
            }
            // Items should also be processed correctly, potentially from DTOs.

            Order createdOrder = orderService.createNewOrder(
                orderRequest.getCustomer(), // Assuming customer object with ID is passed
                orderRequest.getOrderItems(), // Assuming items are somewhat prepared
                orderRequest.getOrderType(),
                orderRequest.getNotes()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (RuntimeException e) { // Catch more specific exceptions
            // Log error
            System.err.println("Error creating order: " + e.getMessage());
            return ResponseEntity.badRequest().body(null); // Provide error details
        }
    }

    // PATCH /api/orders/{id}/status: Update an order's status
    // Example body: { "status": "CONFIRMED" }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        // @RequestBody OrderStatusUpdateDTO statusUpdateDto would be better
        String newStatusStr = statusUpdate.get("status");
        if (newStatusStr == null) {
            return ResponseEntity.badRequest().build(); // Missing status
        }
        try {
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
            Optional<Order> updatedOrder = orderService.updateOrderStatus(id, newStatus);
            return updatedOrder.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Invalid status string
        } catch (IllegalStateException e) { // From service layer if transition is invalid
             return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // PATCH /api/orders/{id}/pickup-time: Update an order's estimated pickup time
    // Example body: { "pickupTime": "2024-07-21T18:30:00" }
    @PatchMapping("/{id}/pickup-time")
    public ResponseEntity<Order> updateOrderPickupTime(@PathVariable Long id, @RequestBody Map<String, String> pickupTimeUpdate) {
        String pickupTimeStr = pickupTimeUpdate.get("pickupTime");
        if (pickupTimeStr == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            LocalDateTime pickupTime = LocalDateTime.parse(pickupTimeStr);
            Optional<Order> updatedOrder = orderService.updateOrderEstimatedPickupTime(id, pickupTime);
            return updatedOrder.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    // POST /api/orders/{id}/items: Add an item to an existing order
    // @RequestBody OrderItemRequestDTO itemRequestDto would be better
    @PostMapping("/{id}/items")
    public ResponseEntity<Order> addItemToOrder(@PathVariable Long id, @RequestBody OrderItem orderItem) {
        // Again, DTOs are preferred for request bodies.
        // Assuming orderItem is somewhat prepared (e.g., has MenuItem with ID and quantity)
        try {
            Optional<Order> updatedOrder = orderService.addItemToOrder(id, orderItem);
            return updatedOrder.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            System.err.println("Error adding item to order: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // DELETE /api/orders/{orderId}/items/{orderItemId}: Remove an item from an order
    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<Order> removeItemFromOrder(@PathVariable Long orderId, @PathVariable Long orderItemId) {
        try {
            Optional<Order> updatedOrder = orderService.removeItemFromOrder(orderId, orderItemId);
            return updatedOrder.map(ResponseEntity::ok)
                               .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
             System.err.println("Error removing item from order: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }


    // DELETE /api/orders/{id}: Delete an order (use with caution, usually prefer cancellation)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            boolean deleted = orderService.deleteOrder(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Cannot delete in current state
        }
    }
}
