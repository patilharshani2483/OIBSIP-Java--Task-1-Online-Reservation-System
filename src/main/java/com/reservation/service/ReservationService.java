
package com.reservation.service;

import com.reservation.entity.Reservation;
import com.reservation.entity.Train;
import com.reservation.entity.User;
import com.reservation.repository.ReservationRepository;
import com.reservation.repository.TrainRepository;
import com.reservation.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            TrainRepository trainRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
    }

    // BOOK A TICKET
    @Transactional
    public Reservation bookReservation(Reservation reservation) {

        if (reservation.getTrainNumber() == null) {
            throw new IllegalArgumentException("Please select a train.");
        }

        if (reservation.getJourneyDate() == null) {
            throw new IllegalArgumentException("Please select a journey date.");
        }

        // Find selected train
        Train train = trainRepository
                .findByTrainNumber(reservation.getTrainNumber())
                .orElseThrow(() ->
                        new IllegalArgumentException("Selected train was not found."));

        String source = train.getSourceStation();
        String destination = train.getDestinationStation();

        if (source == null || source.isBlank()
                || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Selected train has missing station details.");
        }

        // Get logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new IllegalArgumentException(
                    "Please log in before booking a ticket.");
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Logged-in user was not found."));

        // Set train and user details
        reservation.setTrain(train);
        reservation.setTrainNumber(train.getTrainNumber());
        reservation.setTrainName(train.getTrainName());
        reservation.setSourceStation(source);
        reservation.setDestinationStation(destination);
        reservation.setUser(user);

        // Check total seats
        Integer configuredSeats = train.getTotalSeats();

        if (configuredSeats == null || configuredSeats <= 0) {
            throw new IllegalArgumentException(
                    "No seats are configured for this train.");
        }

        int totalSeats = configuredSeats;

        // Fetch only confirmed reservations for this train and date
        List<Reservation> existingReservations =
                reservationRepository
                        .findByTrain_IdAndJourneyDateAndStatusIgnoreCase(
                                train.getId(),
                                reservation.getJourneyDate(),
                                "CONFIRMED"
                        );

        // Collect occupied seat numbers
        Set<String> occupiedSeats = new HashSet<>();

        for (Reservation existing : existingReservations) {
            if (existing.getSeatNumber() != null
                    && !existing.getSeatNumber().isBlank()) {
                occupiedSeats.add(existing.getSeatNumber().trim());
            }
        }

        // Find the first available seat
        String availableSeat = null;

        for (int seat = 1; seat <= totalSeats; seat++) {
            String candidate = String.valueOf(seat);

            if (!occupiedSeats.contains(candidate)) {
                availableSeat = candidate;
                break;
            }
        }

        if (availableSeat == null) {
            throw new IllegalArgumentException(
                    "No seats available for this train on the selected date.");
        }

        // Generate PNR and confirm booking
        reservation.setSeatNumber(availableSeat);
        reservation.setPnr(generateUniquePnr());
        reservation.setStatus("CONFIRMED");

        return reservationRepository.save(reservation);
    }

    // GENERATE UNIQUE 10-DIGIT PNR
    private String generateUniquePnr() {
        String pnr;

        do {
            long number = ThreadLocalRandom.current().nextLong(
                    1_000_000_000L,
                    10_000_000_000L
            );

            pnr = String.valueOf(number);

        } while (reservationRepository.existsByPnr(pnr));

        return pnr;
    }

    // GET ALL RESERVATIONS
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // FIND RESERVATION BY PNR
    @Transactional(readOnly = true)
    public Reservation getReservationByPnr(String pnr) {

        if (pnr == null || pnr.isBlank()) {
            throw new IllegalArgumentException("Please enter a PNR number.");
        }

        return reservationRepository.findByPnr(pnr.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation not found for PNR: " + pnr));
    }

    // CANCEL TICKET AND PERMANENTLY DELETE ITS DATABASE RECORD
    @Transactional
    public void cancelReservation(String pnr) {

        Reservation reservation = getReservationByPnr(pnr);

        if ("CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalArgumentException(
                    "This reservation is already cancelled.");
        }

        reservationRepository.delete(reservation);
    }
}
