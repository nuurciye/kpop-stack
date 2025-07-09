package com.example.somaliaiagent.service;

import com.example.somaliaiagent.model.Customer;
import com.example.somaliaiagent.model.Order;
import com.example.somaliaiagent.model.OrderItem;
import com.example.somaliaiagent.model.OrderStatus;
import com.example.somaliaiagent.model.OrderType;
import com.example.somaliaiagent.repository.OrderRepository;
import com.example.somaliaiagent.repository.CustomerRepository;
import com.example.somaliaiagent.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Import DTOs if you create them, e.g.:
// import com.example.somaliaiagent.dto.OrderRequest;
// import com.example.somaliaiagent.dto.OrderItemRequest;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository; // Or CustomerService
    private final MenuItemRepository menuItemRepository; // Or MenuService

    // Optional: dedicated POS service
    // private final SquareService squareService;


    @Autowired
    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        MenuItemRepository menuItemRepository
                        /* SquareService squareService */) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.menuItemRepository = menuItemRepository;
        // this.squareService = squareService;
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        // TODO: Add pagination and filtering
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        // TODO: Handle customer not found
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId)); // Replace with custom exception
        return orderRepository.findByCustomer(customer);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /*
    // Example using DTOs for order creation
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        Customer customer = customerRepository.findById(orderRequest.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found")); // Replace with specific exception

        Order order = new Order(customer, orderRequest.getOrderType());
        order.setNotes(orderRequest.getNotes());

        List<OrderItem> orderItems = orderRequest.getItems().stream()
                .map(itemReq -> {
                    MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                            .orElseThrow(() -> new RuntimeException("Menu item not found"));
                    if (!menuItem.isAvailable()) {
                        throw new RuntimeException("Menu item " + menuItem.getName() + " is not available.");
                    }
                    return new OrderItem(order, menuItem, itemReq.getQuantity(), itemReq.getNotes());
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems); // This will also calculate total amount
        order.setStatus(OrderStatus.PENDING);
        order.setEstimatedPickupTime(calculateEstimatedPickupTime(order)); // Placeholder

        Order savedOrder = orderRepository.save(order);
        // syncWithPOS(savedOrder); // Placeholder for POS integration
        return savedOrder;
    }
    */

    @Transactional
    public Order createNewOrder(Customer customer, List<OrderItem> items, OrderType orderType, String notes) {
        Order order = new Order(customer, orderType);
        order.setNotes(notes);

        items.forEach(item -> {
            // Ensure menu item is fetched and available if not already done
            if (item.getMenuItem() == null || item.getMenuItem().getId() == null) {
                 throw new IllegalArgumentException("OrderItem must have a valid MenuItem.");
            }
            // Re-fetch or validate menu item from repository if necessary
            menuItemRepository.findById(item.getMenuItem().getId()).filter(MenuItem::isAvailable)
                    .orElseThrow(() -> new RuntimeException("Menu item " + item.getMenuItem().getName() + " is not available or not found."));
            order.addOrderItem(item);
        });

        order.setStatus(OrderStatus.PENDING);
        // order.setEstimatedPickupTime(calculateEstimatedPickupTime(order)); // Placeholder

        Order savedOrder = orderRepository.save(order);
        // syncWithPOS(savedOrder);
        return savedOrder;
    }


    @Transactional
    public Optional<Order> updateOrderStatus(Long orderId, OrderStatus newStatus) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    // TODO: Add logic for valid status transitions
                    // e.g., cannot cancel a completed order
                    order.setStatus(newStatus);
                    if (newStatus == OrderStatus.READY_FOR_PICKUP && order.getEstimatedPickupTime() == null) {
                        // set a default if not already set by a more specific process
                        order.setEstimatedPickupTime(LocalDateTime.now().plusMinutes(15));
                    }
                    return orderRepository.save(order);
                });
    }

    @Transactional
    public Optional<Order> updateOrderEstimatedPickupTime(Long orderId, LocalDateTime pickupTime) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setEstimatedPickupTime(pickupTime);
                    return orderRepository.save(order);
                });
    }


    @Transactional
    public Optional<Order> addItemToOrder(Long orderId, OrderItem orderItem) {
         // Ensure menu item is fetched and available
        menuItemRepository.findById(orderItem.getMenuItem().getId()).filter(MenuItem::isAvailable)
                .orElseThrow(() -> new RuntimeException("Menu item " + orderItem.getMenuItem().getName() + " is not available or not found."));

        return orderRepository.findById(orderId)
            .map(order -> {
                // TODO: Check if order is in a state that allows adding items (e.g., PENDING, CONFIRMED)
                if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
                    order.addOrderItem(orderItem);
                    return orderRepository.save(order);
                } else {
                    throw new IllegalStateException("Cannot add item to order with status: " + order.getStatus());
                }
            });
    }

    @Transactional
    public Optional<Order> removeItemFromOrder(Long orderId, Long orderItemId) {
        return orderRepository.findById(orderId)
            .map(order -> {
                 // TODO: Check if order is in a state that allows removing items
                if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
                    OrderItem itemToRemove = order.getOrderItems().stream()
                        .filter(oi -> oi.getId().equals(orderItemId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("OrderItem not found in order"));
                    order.removeOrderItem(itemToRemove);
                    return orderRepository.save(order);
                } else {
                     throw new IllegalStateException("Cannot remove item from order with status: " + order.getStatus());
                }
            });
    }


    // Placeholder for POS integration logic
    private void syncWithPOS(Order order) {
        System.out.println("Attempting to sync order " + order.getId() + " with POS.");
        // In a real scenario, this would call a SquareService or similar
        // e.g., squareService.createOrder(order);
        // Update order.setPosSyncStatus based on the result
        order.setPosSyncStatus("PENDING_SYNC"); // Example
        // orderRepository.save(order); // If status needs to be persisted immediately
    }

    // Placeholder for calculating estimated pickup time
    private LocalDateTime calculateEstimatedPickupTime(Order order) {
        // Logic based on number of items, current kitchen load, etc.
        // For now, a simple default:
        return LocalDateTime.now().plusMinutes(30); // Default 30 minutes
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersForPOSSync() {
        return orderRepository.findByPosSyncStatus("PENDING_SYNC"); // Or whatever status indicates pending
    }

    @Transactional
    public void markOrderAsSynced(Long orderId, boolean success) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setPosSyncStatus(success ? "SYNCED" : "FAILED");
            orderRepository.save(order);
        });
    }

    // Note: Deleting orders is often discouraged in production systems.
    // Instead, they are typically marked as CANCELED or ARCHIVED.
    // If true deletion is needed:
    @Transactional
    public boolean deleteOrder(Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Add checks: only allow deletion if order is in a deletable state (e.g. PENDING and not paid)
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CANCELED) {
                 orderRepository.deleteById(id);
                 return true;
            } else {
                throw new IllegalStateException("Cannot delete order with status: " + order.getStatus());
            }
        }
        return false;
    }
}
