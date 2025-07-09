package com.example.somaliaiagent.controller;

import com.example.somaliaiagent.model.Reservation;
import com.example.somaliaiagent.model.ReservationStatus;
import com.example.somaliaiagent.service.ReservationService;
// Import DTOs if you create them, e.g.:
// import com.example.somaliaiagent.dto.ReservationRequestDTO;
// import com.example.somaliaiagent.dto.ReservationStatusUpdateDTO;
// import com.example.somaliaiagent.dto.ReservationDetailsUpdateDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;


@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // GET /api/reservations: Get all reservations
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status
    ) {
        List<Reservation> reservations;
        if (customerId != null) {
            reservations = reservationService.getReservationsByCustomerId(customerId);
        } else if (date != null) {
            reservations = reservationService.getReservationsByDate(date);
        } else if (status != null) {
            reservations = reservationService.getReservationsByStatus(status);
        }
        else {
            reservations = reservationService.getAllReservations();
        }
        return ResponseEntity.ok(reservations);
    }

    // GET /api/reservations/{id}: Get a specific reservation by ID
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.getReservationById(id);
        return reservation.map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/reservations: Create a new reservation
    // Ideally use a DTO: @RequestBody ReservationRequestDTO reservationRequest
    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservationRequest) {
        // Simplified for stub, proper DTO and service layer mapping needed.
        // Assumes reservationRequest contains customer ID, time, party size, notes.
        try {
             if (reservationRequest.getCustomer() == null || reservationRequest.getCustomer().getId() == null) {
                return ResponseEntity.badRequest().build(); // Or throw specific exception
            }
            // TODO: Validate reservationTime, partySize
            Reservation newReservation = reservationService.createReservation(reservationRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
        } catch (RuntimeException e) {
            System.err.println("Error creating reservation: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // PATCH /api/reservations/{id}/status: Update a reservation's status
    // Example body: { "status": "CONFIRMED" }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateReservationStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        // Use DTO: @RequestBody ReservationStatusUpdateDTO statusUpdateDto
        String newStatusStr = statusUpdate.get("status");
        if (newStatusStr == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ReservationStatus newStatus = ReservationStatus.valueOf(newStatusStr.toUpperCase());
            Optional<Reservation> updatedReservation = reservationService.updateReservationStatus(id, newStatus);
            return updatedReservation.map(ResponseEntity::ok)
                                     .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Invalid status string
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // PUT /api/reservations/{id}: Update reservation details (time, party size, notes)
    // Example body: { "reservationTime": "2024-07-22T19:00:00", "partySize": 4, "notes": "Window seat if possible" }
    // Better to use PATCH for partial updates with a DTO (ReservationDetailsUpdateDTO)
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservationDetails(@PathVariable Long id, @RequestBody Reservation details) {
        // This is a full update, partial update with PATCH and DTO is often better.
        // Assuming 'details' contains new time, party size, notes.
        try {
            Optional<Reservation> updatedReservation = reservationService.updateReservationDetails(
                    id, details.getReservationTime(), details.getPartySize(), details.getNotes()
            );
            return updatedReservation.map(ResponseEntity::ok)
                                     .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
             return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        catch (RuntimeException e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }


    // POST /api/reservations/{id}/cancel: Cancel a reservation
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        boolean cancelled = reservationService.cancelReservation(id);
        if (cancelled) {
            // Fetch the updated reservation to return it
            Optional<Reservation> updatedReservation = reservationService.getReservationById(id);
            return updatedReservation.map(ResponseEntity::ok)
                                      .orElseGet(() -> ResponseEntity.notFound().build()); // Should not happen if cancelled true
        } else {
            // Could be not found, or not in a cancellable state
            Optional<Reservation> existingReservation = reservationService.getReservationById(id);
            if (existingReservation.isEmpty()) return ResponseEntity.notFound().build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(existingReservation.get()); // Not cancellable
        }
    }

    // DELETE /api/reservations/{id}: Delete a reservation (use with caution)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
         try {
            boolean deleted = reservationService.deleteReservation(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Cannot delete in current state
        }
    }

    // GET /api/reservations/availability?dateTime=...&partySize=...
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam int partySize) {
        // TODO: Implement actual availability check in ReservationService
        boolean available = reservationService.checkAvailability(dateTime, partySize);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
