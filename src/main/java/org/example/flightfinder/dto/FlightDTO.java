package org.example.flightfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightDTO {
    @JsonProperty("from")
    private String from;
    @JsonProperty("to")
    private String to;
    @JsonProperty("airline")
    private String airline;
    @JsonProperty("departure")
    private String departure;
    @JsonProperty("arrival")
    private String arrival;
}
