package com.example.somaliaiagent.repository;

import com.example.somaliaiagent.model.Customer;
import com.example.somaliaiagent.model.Reservation;
import com.example.somaliaiagent.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCustomer(Customer customer);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByReservationTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Reservation> findByReservationTimeGreaterThanEqual(LocalDateTime time);

    List<Reservation> findByCustomerAndStatus(Customer customer, ReservationStatus status);

    // Example of a query to find upcoming confirmed reservations for a specific date
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND FUNCTION('DATE', r.reservationTime) = FUNCTION('DATE', :dateTime)")
    List<Reservation> findReservationsByStatusAndDate(@Param("status") ReservationStatus status, @Param("dateTime") LocalDateTime dateTime);

    // Find reservations for a specific customer within a date range
    List<Reservation> findByCustomerAndReservationTimeBetween(Customer customer, LocalDateTime startTime, LocalDateTime endTime);
}
