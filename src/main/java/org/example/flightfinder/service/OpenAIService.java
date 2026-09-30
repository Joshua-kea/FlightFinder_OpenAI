package org.example.flightfinder.service;

import org.example.flightfinder.dto.FlightOptionDTO;
import org.example.flightfinder.model.FlightOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openAIKey;

    @Value("${openai.url}")
    private String url;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.temperature}")
    private double temperature;

    @Value("${openai.max_tokens}")
    private int maxTokens;

    private WebClient webClient;

    public OpenAIService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.build();
    }

    public List<String> aiResolveAirports(String input){

        String prompt = "Convert this location into 1-3 major IATA airport codes. " +
                "Only return comma separated codes, no explanation. \n\n" +
                "Input: " + input;

        String response = makeRequest(prompt,
                "You are an airport code converter.");

        return List.of(response.split(","))
                .stream()
                .map(String::trim)
                .filter(code -> code.length() == 3)
                .map(String::toUpperCase)
                .limit(2)
                .toList();
    }

    public String makeRequest(String userPrompt, String systemMessageText){

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role","system");
        systemMessage.put("content", systemMessageText);
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);

        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("max_tokens",maxTokens);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(body);

            Map response = webClient.post()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + openAIKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List choices = (List) response.get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map message = (Map) firstChoice.get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            throw new RuntimeException("Error calling OpenAI: " + e.getMessage());
        }
    }

    public List<FlightOptionDTO> generateResponse(List<FlightOption> flights, String preference){

        if(flights == null || flights.isEmpty()){
            return List.of();
        }

        StringBuilder flightText = new StringBuilder();

        for(FlightOption flight : flights){
            flightText.append("Flight:\n");
            flightText.append(flight.getRouteDetails()).append("\n");

            flightText.append("Total duration: ")
                    .append(flight.getDuration() / 60)
                    .append("h ")
                    .append(flight.getDuration() % 60)
                    .append("m\n");

            flightText.append("Price: ")
                    .append(flight.getPrice())
                    .append(" DKK\n\n");
        }

        String prompt =
                "Return ONLY valid JSON, no text before or after. Do not use Markdown.\n\n" +
                        "User preference: " + preference + "\n\n" +
                        "Select EXACTLY 3 best flight options from the data below.\n\n" +
                        "Rules:\n" +
                        "- Use ONLY provided flight data, do NOT invent anything\n" +
                        "- departure/arrival in flights must be TIME ONLY (e.g. 14:15), no dates\n" +
                        "- Each flight object must have ONLY: from, to, airline, departure, arrival\n\n" +
                        "Each option must have: flights[], layover, total_duration, price, reason, description\n\n" +
                        "Description format (use \\n for line breaks, NO 'Option X:' prefix):\n" +
                        "From [first.from] to [last.to] via [stop airports].\n" +
                        "Total travel time is [total_duration].\n" +
                        "For EACH flight segment write:\n" +
                        "You depart from [segment.from] at [segment.departure] and arrive in [segment.to] at [segment.arrival] ([segment.airline]).\n" +
                        "If there is another segment after this one, write: The layover is [layover].\n" +
                        "Repeat this for every segment in the flights array.\n" +
                        "Total price is [price].\n" +
                        "[reason]\n\n" +
                        "Flights:\n" + flightText;

        String aiResponse = makeRequest(prompt, "You are a flight recommendation assistant");

        try{
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(
                    aiResponse, mapper.getTypeFactory().constructCollectionType(List.class, FlightOptionDTO.class));
        } catch (Exception e){
            throw new RuntimeException("Failed to parse AI JSON " + e.getMessage());
        }
    }

}
