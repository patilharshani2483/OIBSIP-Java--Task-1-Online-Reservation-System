
package com.reservation.controller;

import com.reservation.entity.Reservation;
import com.reservation.repository.ReservationRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PnrController {

    private final ReservationRepository reservationRepository;

    public PnrController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // Open PNR status page
    @GetMapping("/pnr-status")
    public String showPnrPage(Model model) {
        return "pnr-status";
    }

    // Search reservation using PNR
    @PostMapping("/pnr-status")
    public String checkPnr(
            @RequestParam(name = "pnr", defaultValue = "") String pnr,
            Model model) {

        pnr = pnr.trim();
        model.addAttribute("searchedPnr", pnr);

        // Validate 10-digit PNR
        if (!pnr.matches("\\d{10}")) {
            model.addAttribute(
                    "error",
                    "Please enter a valid 10-digit PNR number."
            );
            return "pnr-status";
        }

        // Find reservation
        Reservation reservation = reservationRepository
                .findByPnr(pnr)
                .orElse(null);

        if (reservation == null) {
            model.addAttribute(
                    "error",
                    "Reservation not found. Please check the number and try again."
            );
            return "pnr-status";
        }

        // Show reservation details
        model.addAttribute("reservation", reservation);

        return "pnr-status";
    }
}
