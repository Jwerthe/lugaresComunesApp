package com.example.lugarescomunes.models.api;

import java.math.BigDecimal;

public class RoutePointRequest {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer stepOrder;
    private String instruction;
    private String landmark;

    // Constructors
    public RoutePointRequest() {}

    public RoutePointRequest(BigDecimal latitude, BigDecimal longitude, Integer stepOrder) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.stepOrder = stepOrder;
    }

    // Getters y setters
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
}