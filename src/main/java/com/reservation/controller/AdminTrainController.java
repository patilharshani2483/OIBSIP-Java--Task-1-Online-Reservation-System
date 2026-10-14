
package com.reservation.controller;

import com.reservation.entity.Train;
import com.reservation.repository.TrainRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/trains")
public class AdminTrainController {

    private final TrainRepository trainRepository;

    public AdminTrainController(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    // Display train management page
    @GetMapping
    public String list(Model model) {
        model.addAttribute("trains", trainRepository.findAll());
        model.addAttribute("train", new Train());

        return "admin-trains";
    }

    // Add a new train or update an existing train
    @PostMapping
    public String save(
            @ModelAttribute("train") Train train,
            RedirectAttributes flash) {

        if (train.getTrainNumber() == null
                || train.getTrainNumber() < 1
                || train.getTrainName() == null
                || train.getTrainName().isBlank()
                || train.getSourceStation() == null
                || train.getSourceStation().isBlank()
                || train.getDestinationStation() == null
                || train.getDestinationStation().isBlank()
                || train.getTotalSeats() == null
                || train.getTotalSeats() < 1) {

            flash.addFlashAttribute(
                    "error",
                    "Enter valid train details and seat capacity."
            );

            return "redirect:/admin/trains";
        }

        if (train.getId() != null
                && !trainRepository.existsById(train.getId())) {

            flash.addFlashAttribute(
                    "error",
                    "Train record not found. Please refresh and try again."
            );

            return "redirect:/admin/trains";
        }

        train.setTrainName(train.getTrainName().trim());
        train.setSourceStation(train.getSourceStation().trim());
        train.setDestinationStation(train.getDestinationStation().trim());

        trainRepository.save(train);

        flash.addFlashAttribute(
                "message",
                "Train saved successfully."
        );

        return "redirect:/admin/trains";
    }

    // Delete a train
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes flash) {

        if (!trainRepository.existsById(id)) {
            flash.addFlashAttribute("error", "Train not found.");
        } else {
            try {
                trainRepository.deleteById(id);

                flash.addFlashAttribute(
                        "message",
                        "Train deleted successfully."
                );

            } catch (Exception ex) {
                flash.addFlashAttribute(
                        "error",
                        "Train cannot be deleted while reservations reference it."
                );
            }
        }

        return "redirect:/admin/trains";
    }
}
