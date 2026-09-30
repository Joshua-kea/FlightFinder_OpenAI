package org.example.flightfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlightOptionDTO {
    @JsonProperty("flights")
    private List<FlightDTO> flights;
    @JsonProperty("layover")
    private String layover;
    @JsonProperty("total_duration")
    private String totalDuration;
    @JsonProperty("price")
    private String price;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("description")
    private String description;
}
