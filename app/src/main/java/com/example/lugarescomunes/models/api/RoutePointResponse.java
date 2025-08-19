package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class RoutePointResponse {
    private String id;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer stepOrder;
    private String instruction;
    private String landmark;
    @SerializedName("estimatedTimeFromPreviousSeconds")
    private Integer estimatedTimeFromPreviousSeconds;

    // Constructors
    public RoutePointResponse() {}

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Integer getEstimatedTimeFromPreviousSeconds() {
        return estimatedTimeFromPreviousSeconds;
    }

    public void setEstimatedTimeFromPreviousSeconds(Integer estimatedTimeFromPreviousSeconds) {
        this.estimatedTimeFromPreviousSeconds = estimatedTimeFromPreviousSeconds;
    }
}
