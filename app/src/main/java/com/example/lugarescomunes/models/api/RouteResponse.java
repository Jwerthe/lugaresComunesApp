package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

public class RouteResponse {
    private String id;
    private String name;
    private String description;

    @SerializedName("destinationPlace")
    private PlaceResponse destinationPlace;

    @SerializedName("estimatedTimeMinutes")
    private Integer estimatedTimeMinutes;

    @SerializedName("distanceMeters")
    private Double distanceMeters;

    private String difficulty;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("totalRatings")
    private Integer totalRatings;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    // Constructors
    public RouteResponse() {}

    // Getters y setters básicos
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PlaceResponse getDestinationPlace() { return destinationPlace; }
    public void setDestinationPlace(PlaceResponse destinationPlace) { this.destinationPlace = destinationPlace; }

    public Integer getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}