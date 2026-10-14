
package com.reservation.repository;

import com.reservation.entity.Reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    // Find reservation by PNR
    Optional<Reservation> findByPnr(String pnr);

    // Check whether PNR already exists
    boolean existsByPnr(String pnr);

    List<Reservation> findByUser_UsernameOrderByJourneyDateDesc(String username);

    // Find confirmed reservations for the same train and journey date
    List<Reservation> findByTrain_IdAndJourneyDateAndStatusIgnoreCase(
            Long trainId,
            LocalDate journeyDate,
            String status
    );
}
