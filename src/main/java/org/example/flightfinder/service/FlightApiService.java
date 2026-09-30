package org.example.flightfinder.service;

import org.example.flightfinder.model.FlightOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightApiService {

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.host}")
    private String host;

    private WebClient webClient;

    public FlightApiService(WebClient.Builder builder, @Value("${rapidapi.url}") String baseUrl) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public List<FlightOption> search(String from, String to, LocalDate date) {

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/searchFlights")
                        .queryParam("departure_id", from)
                        .queryParam("arrival_id", to)
                        .queryParam("outbound_date", date.toString())
                        .queryParam("adults", "1")
                        .queryParam("currency", "DKK")
                        .build())
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<FlightOption> flights = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode data = root.path("data");
            JsonNode itineraries = data.path("itineraries");
            JsonNode topFlights = itineraries.path("topFlights");
            JsonNode otherFlights = itineraries.path("otherFlights");

            for (int i = 0; i < topFlights.size(); i++) {
                flights.add(parseFlight(topFlights.get(i), from, to));
            }

            for (int i = 0; i < otherFlights.size(); i++) {
                flights.add(parseFlight(otherFlights.get(i), from, to));
            }

        } catch (Exception e) {
            throw new RuntimeException("Parsing error: " + e.getMessage());
        }
        return flights;

    }

    public FlightOption parseFlight(JsonNode flight, String from, String to) {

        int price = flight.path("price").asInt();
        int duration = flight.path("duration").path("raw").asInt();

        JsonNode flightsNode = flight.path("flights");

        StringBuilder routeDetails = new StringBuilder();

        for (int j = 0; j < flightsNode.size(); j++) {
            JsonNode segment = flightsNode.get(j);

            String fromAirport = segment.path("departure_airport").path("airport_code").asText();
            String toAirport = segment.path("arrival_airport").path("airport_code").asText();
            String airlineName = segment.path("airline").asText();

            String departureTime = segment.path("departure_airport").path("time").asText();
            String arrivalTime = segment.path("arrival_airport").path("time").asText();

            if (fromAirport.isEmpty() || toAirport.isEmpty()) {
                continue;
            }

            routeDetails.append("Flight: ")
                    .append(fromAirport)
                    .append(" → ")
                    .append(toAirport)
                    .append("\nAirline: ")
                    .append(airlineName)
                    .append("\nDeparture: ")
                    .append(departureTime)
                    .append("\nArrival: ")
                    .append(arrivalTime)
                    .append("\n\n");
        }

        return new FlightOption(
                from + " -> " + to,
                price,
                duration,
                routeDetails.toString()
        );
    }
}
