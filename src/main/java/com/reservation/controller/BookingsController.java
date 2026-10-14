package com.reservation.controller;
import com.reservation.repository.ReservationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class BookingsController {
 private final ReservationRepository reservationRepository;
 public BookingsController(ReservationRepository reservationRepository) { this.reservationRepository = reservationRepository; }
 @GetMapping("/my-bookings")
 public String myBookings(Model model, Authentication authentication) 
 {
  model.addAttribute("reservations", reservationRepository.findByUser_UsernameOrderByJourneyDateDesc(authentication.getName()));
  return "my-bookings";
 }
}
