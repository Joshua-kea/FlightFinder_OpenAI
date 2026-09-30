package org.example.flightfinder.service;

import org.example.flightfinder.dto.FlightOptionDTO;
import org.example.flightfinder.model.FlightOption;
import org.example.flightfinder.model.SearchInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FlightService {

    @Autowired
    private FlightApiService flightApiService;
    @Autowired
    private OpenAIService openAIService;

    public List<FlightOptionDTO> search(SearchInput input) {

       List<String> fromAirports = openAIService.aiResolveAirports(input.getDeparture());
       List<String> toAirports = openAIService.aiResolveAirports(input.getArrival());

        if(fromAirports.isEmpty() || toAirports.isEmpty()){
            throw new RuntimeException("Could not find airports");
        }

        List<FlightOption> allFlights = new ArrayList<>();

        for(String from : fromAirports){
            for (String to : toAirports){
                allFlights.addAll(flightApiService.search(from, to, input.getDate()));
            }
        }

        List<FlightOption> sortedFlights = applyPreference(allFlights, input.getPreference());

        return openAIService.generateResponse(
                sortedFlights.subList(0, Math.min(3, sortedFlights.size())),
                input.getPreference()
        );
    }

    private List<FlightOption> applyPreference (List<FlightOption> flights, String preference){

        switch (preference.toLowerCase()){

            case "cheapest":
                flights.sort(Comparator.comparingInt(FlightOption::getPrice));
                break;

            case "fastest":
                flights.sort(Comparator.comparingInt(FlightOption::getDuration));
                break;

            case "best balanced":
                flights.sort(Comparator.comparingInt(
                        flight -> flight.getPrice() + flight.getDuration() *2));
                break;
        }
        return flights;
    }
}