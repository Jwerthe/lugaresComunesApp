package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class StartNavigationRequest {
    @SerializedName("routeId")
    private String routeId;
    @SerializedName("startLatitude")
    private BigDecimal startLatitude;
    @SerializedName("startLongitude")
    private BigDecimal startLongitude;

    // Constructors
    public StartNavigationRequest() {}

    public StartNavigationRequest(String routeId, BigDecimal startLatitude, BigDecimal startLongitude) {
        this.routeId = routeId;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
    }

    // Getters y setters
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public BigDecimal getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(BigDecimal startLatitude) {
        this.startLatitude = startLatitude;
    }

    public BigDecimal getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(BigDecimal startLongitude) {
        this.startLongitude = startLongitude;
    }
}