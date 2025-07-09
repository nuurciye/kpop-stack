package com.example.somaliaiagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime; // Date and time of reservation

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // e.g., "window seat preferred", "birthday celebration"

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status; // PENDING, CONFIRMED, CANCELED, COMPLETED (attended)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Reservation() {
        this.status = ReservationStatus.PENDING;
    }

    public Reservation(Customer customer, LocalDateTime reservationTime, Integer partySize, String notes) {
        this();
        this.customer = customer;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.notes = notes;
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

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    public Integer getPartySize() {
        return partySize;
    }

    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
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

    // Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // toString
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", reservationTime=" + reservationTime +
                ", partySize=" + partySize +
                ", status=" + status +
                '}';
    }
}

enum ReservationStatus {
    PENDING,    // Reservation requested, awaiting confirmation
    CONFIRMED,  // Reservation confirmed by restaurant
    CANCELED,   // Reservation canceled by customer or restaurant
    COMPLETED,  // Customer attended
    NO_SHOW     // Customer did not show up
}
