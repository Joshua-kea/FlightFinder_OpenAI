package org.example.flightfinder.controller;

import org.example.flightfinder.dto.FlightOptionDTO;
import org.example.flightfinder.model.SearchInput;
import org.example.flightfinder.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:63342")
@RestController
public class FlightController {

    @Autowired
    private FlightService flightService;


    @PostMapping("/search")
    public List<FlightOptionDTO> search(@RequestBody SearchInput input){
        return flightService.search(input);
    }

}
