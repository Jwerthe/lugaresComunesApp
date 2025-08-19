package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

public class NavigationResponse {
    private String id;
    @SerializedName("routeId")
    private String routeId;
    @SerializedName("startTime")
    private String startTime;
    @SerializedName("endTime")
    private String endTime;
    @SerializedName("wasSuccessful")
    private Boolean wasSuccessful;

    // Constructors
    public NavigationResponse() {}

    public NavigationResponse(String id, String routeId) {
        this.id = id;
        this.routeId = routeId;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Boolean getWasSuccessful() {
        return wasSuccessful;
    }

    public void setWasSuccessful(Boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }
}
