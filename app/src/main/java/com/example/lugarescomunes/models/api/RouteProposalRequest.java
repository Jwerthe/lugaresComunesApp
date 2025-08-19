package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RouteProposalRequest {
    private String name;
    private String description;
    @SerializedName("destinationPlaceId")
    private String destinationPlaceId;
    private List<RoutePointRequest> points;

    // Constructors
    public RouteProposalRequest() {}

    public RouteProposalRequest(String name, String description, String destinationPlaceId) {
        this.name = name;
        this.description = description;
        this.destinationPlaceId = destinationPlaceId;
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDestinationPlaceId() {
        return destinationPlaceId;
    }

    public void setDestinationPlaceId(String destinationPlaceId) {
        this.destinationPlaceId = destinationPlaceId;
    }

    public List<RoutePointRequest> getPoints() {
        return points;
    }

    public void setPoints(List<RoutePointRequest> points) {
        this.points = points;
    }
}
