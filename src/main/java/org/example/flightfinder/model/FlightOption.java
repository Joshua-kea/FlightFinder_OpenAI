package org.example.flightfinder.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightOption {

    private String route;
    private int price;
    private int duration;
    private String routeDetails;

    public FlightOption(String route, int price, int duration, String routeDetails){
        this.route = route;
        this.price = price;
        this.duration = duration;
        this.routeDetails = routeDetails;
    }
}
