package org.example.flightfinder.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SearchInput {

    private String departure;
    private String arrival;
    private LocalDate date;
    private String preference;
}
