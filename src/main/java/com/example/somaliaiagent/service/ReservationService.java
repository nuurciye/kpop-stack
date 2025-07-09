package com.example.somaliaiagent.service;

import com.example.somaliaiagent.model.Customer;
import com.example.somaliaiagent.model.Reservation;
import com.example.somaliaiagent.model.ReservationStatus;
import com.example.somaliaiagent.repository.ReservationRepository;
import com.example.somaliaiagent.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository; // Or CustomerService

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, CustomerRepository customerRepository) {
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        // TODO: Add pagination and filtering
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId)); // Use custom exception
        return reservationRepository.findByCustomer(customer);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return reservationRepository.findByReservationTimeBetween(startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getUpcomingReservations() {
        return reservationRepository.findByReservationTimeGreaterThanEqual(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Reservation> getConfirmedReservationsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        // Using direct datetime for the query as per repository method
        return reservationRepository.findReservationsByStatusAndDate(ReservationStatus.CONFIRMED, startOfDay);
    }


    @Transactional
    public Reservation createReservation(Long customerId, LocalDateTime reservationTime, int partySize, String notes) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId)); // Use custom exception

        // TODO: Add validation for reservationTime (e.g., not in the past, within business hours)
        // TODO: Add validation for partySize (e.g., within restaurant capacity for that time slot)
        // TODO: Check for availability / conflicts before creating

        Reservation reservation = new Reservation(customer, reservationTime, partySize, notes);
        reservation.setStatus(ReservationStatus.PENDING); // Initial status
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
         if (reservation.getCustomer() == null || reservation.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Reservation must have a valid customer.");
        }
        customerRepository.findById(reservation.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + reservation.getCustomer().getId()));

        // TODO: Add validation for reservationTime (e.g., not in the past, within business hours)
        // TODO: Add validation for partySize (e.g., within restaurant capacity for that time slot)
        // TODO: Check for availability / conflicts before creating
        reservation.setStatus(ReservationStatus.PENDING);
        return reservationRepository.save(reservation);
    }


    @Transactional
    public Optional<Reservation> updateReservationStatus(Long reservationId, ReservationStatus newStatus) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    // TODO: Add logic for valid status transitions
                    reservation.setStatus(newStatus);
                    return reservationRepository.save(reservation);
                });
    }

    @Transactional
    public Optional<Reservation> updateReservationDetails(Long reservationId, LocalDateTime newTime, Integer newPartySize, String newNotes) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    // TODO: Add validation and conflict checks if time/party size changes
                    if (reservation.getStatus() == ReservationStatus.PENDING || reservation.getStatus() == ReservationStatus.CONFIRMED) {
                        if (newTime != null) {
                            // Validate newTime (e.g. not in past, business hours)
                            reservation.setReservationTime(newTime);
                        }
                        if (newPartySize != null) {
                            // Validate newPartySize
                            reservation.setPartySize(newPartySize);
                        }
                        if (newNotes != null) {
                            reservation.setNotes(newNotes);
                        }
                        return reservationRepository.save(reservation);
                    } else {
                        throw new IllegalStateException("Cannot update reservation with status: " + reservation.getStatus());
                    }
                });
    }

    @Transactional
    public boolean cancelReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    // Allow cancellation if PENDING or CONFIRMED
                    if (reservation.getStatus() == ReservationStatus.PENDING || reservation.getStatus() == ReservationStatus.CONFIRMED) {
                        reservation.setStatus(ReservationStatus.CANCELED);
                        reservationRepository.save(reservation);
                        return true;
                    }
                    // Potentially allow cancellation for other statuses based on business rules
                    return false;
                }).orElse(false);
    }

    // Placeholder for checking table availability
    public boolean checkAvailability(LocalDateTime dateTime, int partySize) {
        // This would involve more complex logic:
        // 1. Get restaurant's operating hours and table configuration.
        // 2. Get all confirmed reservations around that dateTime.
        // 3. Calculate current occupancy and see if partySize can be accommodated.
        // For now, assume always available as a stub.
        System.out.println("Checking availability for: " + dateTime + ", party size: " + partySize);
        return true;
    }

    // Note: Deleting reservations is often discouraged. Prefer CANCELED status.
    // If true deletion is needed:
    @Transactional
    public boolean deleteReservation(Long id) {
         Optional<Reservation> resOpt = reservationRepository.findById(id);
        if (resOpt.isPresent()) {
            Reservation reservation = resOpt.get();
            // Only allow deletion for certain statuses, e.g., if it was never confirmed or long past and canceled
            if (reservation.getStatus() == ReservationStatus.PENDING || reservation.getStatus() == ReservationStatus.CANCELED) {
                reservationRepository.deleteById(id);
                return true;
            } else {
                 throw new IllegalStateException("Cannot delete reservation with status: " + reservation.getStatus());
            }
        }
        return false;
    }
}
