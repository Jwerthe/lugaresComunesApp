package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class CompleteNavigationRequest {
    @SerializedName("navigationSessionId")
    private String navigationSessionId;
    @SerializedName("endLatitude")
    private BigDecimal endLatitude;
    @SerializedName("endLongitude")
    private BigDecimal endLongitude;
    @SerializedName("actualTimeMinutes")
    private Integer actualTimeMinutes;
    @SerializedName("wasSuccessful")
    private Boolean wasSuccessful;

    // Constructors
    public CompleteNavigationRequest() {}

    public CompleteNavigationRequest(String navigationSessionId, BigDecimal endLatitude, BigDecimal endLongitude) {
        this.navigationSessionId = navigationSessionId;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
    }

    // Getters y setters
    public String getNavigationSessionId() {
        return navigationSessionId;
    }

    public void setNavigationSessionId(String navigationSessionId) {
        this.navigationSessionId = navigationSessionId;
    }

    public BigDecimal getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(BigDecimal endLatitude) {
        this.endLatitude = endLatitude;
    }

    public BigDecimal getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(BigDecimal endLongitude) {
        this.endLongitude = endLongitude;
    }

    public Integer getActualTimeMinutes() {
        return actualTimeMinutes;
    }

    public void setActualTimeMinutes(Integer actualTimeMinutes) {
        this.actualTimeMinutes = actualTimeMinutes;
    }

    public Boolean getWasSuccessful() {
        return wasSuccessful;
    }

    public void setWasSuccessful(Boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }
}