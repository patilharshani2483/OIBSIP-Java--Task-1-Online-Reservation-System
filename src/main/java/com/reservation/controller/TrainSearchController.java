
package com.reservation.controller;

import com.reservation.entity.Train;
import com.reservation.repository.TrainRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TrainSearchController 
{

    private final TrainRepository trainRepository;

    public TrainSearchController(TrainRepository trainRepository)
    {
        this.trainRepository = trainRepository;
    }

    @GetMapping("/trains/search")
    public String search(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            Model model) {

        List<Train> trains;

        boolean searching =
                source != null && !source.isBlank()
                && destination != null && !destination.isBlank();

        if (searching)
        {
            trains = trainRepository
                    .findBySourceStationIgnoreCaseAndDestinationStationIgnoreCase(
                            source.trim(), destination.trim());
        }
        else 
        {
            trains = trainRepository.findAll();
        }

        model.addAttribute("trains", trains);
        model.addAttribute("source", source == null ? "" : source);
        model.addAttribute("destination",
                destination == null ? "" : destination);
        model.addAttribute("searching", searching);

        return "train-search";
    }
}
