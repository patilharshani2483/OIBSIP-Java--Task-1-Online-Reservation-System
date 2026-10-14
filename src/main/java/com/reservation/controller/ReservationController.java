
package com.reservation.controller;

import com.reservation.entity.Reservation;
import com.reservation.repository.TrainRepository;
import com.reservation.service.ReservationService;

import jakarta.validation.Valid;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
public class ReservationController {

    private final ReservationService reservationService;
    private final TrainRepository trainRepository;

    public ReservationController(
            ReservationService reservationService,
            TrainRepository trainRepository) {
        this.reservationService = reservationService;
        this.trainRepository = trainRepository;
    }

    // Show reservation form
    @GetMapping("/reservation")
    public String reservationForm(Model model) {

        if (!model.containsAttribute("reservation")) {
            model.addAttribute("reservation", new Reservation());
        }

        model.addAttribute("trains", trainRepository.findAll());

        return "reservation";
    }

    // Show booking page
    @GetMapping("/book")
    public String showBookingPage(Model model) {

        if (!model.containsAttribute("reservation")) {
            model.addAttribute("reservation", new Reservation());
        }

        model.addAttribute("trains", trainRepository.findAll());

        return "reservation";
    }

    // Book a ticket
    @PostMapping("/book")
    public String book(
            @Valid @ModelAttribute("reservation") Reservation reservation,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("trains", trainRepository.findAll());
            return "reservation";
        }

        try {
            Reservation saved =
                    reservationService.bookReservation(reservation);

            model.addAttribute("reservation", saved);

            return "confirmation";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("businessError", ex.getMessage());
            model.addAttribute("trains", trainRepository.findAll());

            return "reservation";
        }
    }

    // Show cancellation page
    @GetMapping("/cancel")
    public String cancelPage(Model model) {

        model.addAttribute("reservation", null);

        return "cancel-reservation";
    }

    // Find reservation using PNR
    @PostMapping("/cancel/fetch")
    public String fetchReservation(
            @RequestParam(name = "pnr", defaultValue = "") String pnr,
            Model model) {

        pnr = pnr.trim();
        model.addAttribute("searchedPnr", pnr);

        try {
            Reservation reservation =
                    reservationService.getReservationByPnr(pnr);

            model.addAttribute("reservation", reservation);

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("reservation", null);
        }

        return "cancel-reservation";
    }

    // Confirm cancellation
    @PostMapping("/cancel/confirm")
    public String confirmCancellation(
            @RequestParam(name = "pnr") String pnr,
            @RequestParam(name = "confirmed", defaultValue = "false")
            boolean confirmed,
            RedirectAttributes redirectAttributes) {

        if (!confirmed) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Cancellation was not confirmed."
            );

            return "redirect:/cancel";
        }

        try {
            reservationService.cancelReservation(pnr.trim());

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Reservation cancelled successfully."
            );

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/cancel";
    }

    // Download ticket as PDF
    @GetMapping("/ticket/download/{pnr}")
    public ResponseEntity<byte[]> downloadTicket(
            @PathVariable String pnr) throws IOException {

        Reservation reservation =
                reservationService.getReservationByPnr(pnr.trim());

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font boldFont = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

            PDType1Font regularFont = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                // Heading
                content.beginText();
                content.setFont(boldFont, 20);
                content.newLineAtOffset(60, 770);
                content.showText("ONLINE RESERVATION SYSTEM");
                content.endText();

                // Ticket title
                content.beginText();
                content.setFont(boldFont, 16);
                content.newLineAtOffset(60, 730);
                content.showText("TRAIN TICKET");
                content.endText();

                // PNR
                content.beginText();
                content.setFont(boldFont, 13);
                content.newLineAtOffset(60, 700);
                content.showText(
                        "PNR: " + safe(reservation.getPnr())
                );
                content.endText();

                String[][] details = {
                        {"Passenger Name", reservation.getPassengerName()},
                        {"Age", String.valueOf(reservation.getAge())},
                        {"Train Number", String.valueOf(reservation.getTrainNumber())},
                        {"Train Name", reservation.getTrainName()},
                        {"Journey Date", String.valueOf(reservation.getJourneyDate())},
                        {"From", reservation.getSourceStation()},
                        {"To", reservation.getDestinationStation()},
                        {"Class", reservation.getClassType()},
                        {"Seat Number", reservation.getSeatNumber()},
                        {"Booking Status", reservation.getStatus()}
                };

                float y = 660;

                for (String[] detail : details) {

                    content.beginText();
                    content.setFont(boldFont, 11);
                    content.newLineAtOffset(60, y);
                    content.showText(safe(detail[0]) + ":");
                    content.endText();

                    content.beginText();
                    content.setFont(regularFont, 11);
                    content.newLineAtOffset(200, y);
                    content.showText(safe(detail[1]));
                    content.endText();

                    y -= 30;
                }

                content.beginText();
                content.setFont(regularFont, 10);
                content.newLineAtOffset(60, 330);
                content.showText(
                        "Please carry this ticket during your journey."
                );
                content.endText();
            }

            document.save(output);

            String filename =
                    "ticket-" + safe(reservation.getPnr()) + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\""
                    )
                    .body(output.toByteArray());
        }
    }

    // Handle null values and unsupported characters
    private String safe(String value) {

        if (value == null || value.isBlank()) {
            return "N/A";
        }

        return value.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
