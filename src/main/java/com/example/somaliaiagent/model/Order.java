package com.example.somaliaiagent.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status; // PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, COMPLETED, CANCELED

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType; // TAKEOUT, DELIVERY (future)

    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Customer notes for the order

    @Column(name = "pos_sync_status")
    private String posSyncStatus; // e.g., PENDING, SYNCED, FAILED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Order() {
        this.status = OrderStatus.PENDING;
        this.orderType = OrderType.TAKEOUT; // Default
        this.totalAmount = BigDecimal.ZERO;
    }

    public Order(Customer customer, OrderType orderType) {
        this();
        this.customer = customer;
        this.orderType = orderType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        calculateTotalAmount(); // Recalculate when items change
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public LocalDateTime getEstimatedPickupTime() {
        return estimatedPickupTime;
    }

    public void setEstimatedPickupTime(LocalDateTime estimatedPickupTime) {
        this.estimatedPickupTime = estimatedPickupTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPosSyncStatus() {
        return posSyncStatus;
    }

    public void setPosSyncStatus(String posSyncStatus) {
        this.posSyncStatus = posSyncStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                                     .map(OrderItem::getSubtotal)
                                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalAmount(); // Ensure total is calculated before first save
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalAmount(); // Ensure total is calculated before update
    }

    // toString
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", orderType=" + orderType +
                ", createdAt=" + createdAt +
                '}';
    }
}

enum OrderStatus {
    PENDING,        // Order placed by customer, awaiting confirmation
    CONFIRMED,      // Order confirmed by restaurant
    PREPARING,      // Order is being prepared
    READY_FOR_PICKUP, // Order is ready for customer pickup
    COMPLETED,      // Order picked up and paid
    CANCELED        // Order canceled
}

enum OrderType {
    TAKEOUT,
    DELIVERY // For future expansion
}
