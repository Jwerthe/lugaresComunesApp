package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

public class RouteResponse {
    private String id;
    private String name;
    private String description;

    @SerializedName("fromLatitude")
    private Double fromLatitude;

    @SerializedName("fromLongitude")
    private Double fromLongitude;

    @SerializedName("fromDescription")
    private String fromDescription;

    @SerializedName("toPlace")
    private PlaceResponse toPlace;

    @SerializedName("totalDistance")
    private Integer totalDistance; // en metros

    @SerializedName("estimatedTime")
    private Integer estimatedTime; // en minutos

    private String difficulty;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("createdBy")
    private UserResponse createdBy;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("totalRatings")
    private Integer totalRatings;

    @SerializedName("timesUsed")
    private Integer timesUsed;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("ratingText")
    private String ratingText;

    @SerializedName("difficultyText")
    private String difficultyText;

    @SerializedName("formattedDistance")
    private String formattedDistance;

    @SerializedName("formattedTime")
    private String formattedTime;

    @SerializedName("popular")
    private Boolean popular;

    @SerializedName("wellRated")
    private Boolean wellRated;

    // Constructor
    public RouteResponse() {}

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getFromLatitude() { return fromLatitude; }
    public void setFromLatitude(Double fromLatitude) { this.fromLatitude = fromLatitude; }

    public Double getFromLongitude() { return fromLongitude; }
    public void setFromLongitude(Double fromLongitude) { this.fromLongitude = fromLongitude; }

    public String getFromDescription() { return fromDescription; }
    public void setFromDescription(String fromDescription) { this.fromDescription = fromDescription; }

    public PlaceResponse getToPlace() { return toPlace; }
    public void setToPlace(PlaceResponse toPlace) { this.toPlace = toPlace; }

    public Integer getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Integer totalDistance) { this.totalDistance = totalDistance; }

    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public UserResponse getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserResponse createdBy) { this.createdBy = createdBy; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }

    public Integer getTimesUsed() { return timesUsed; }
    public void setTimesUsed(Integer timesUsed) { this.timesUsed = timesUsed; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getRatingText() { return ratingText; }
    public void setRatingText(String ratingText) { this.ratingText = ratingText; }

    public String getDifficultyText() { return difficultyText; }
    public void setDifficultyText(String difficultyText) { this.difficultyText = difficultyText; }

    public String getFormattedDistance() { return formattedDistance; }
    public void setFormattedDistance(String formattedDistance) { this.formattedDistance = formattedDistance; }

    public String getFormattedTime() { return formattedTime; }
    public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }

    public Boolean getPopular() { return popular; }
    public void setPopular(Boolean popular) { this.popular = popular; }

    public Boolean getWellRated() { return wellRated; }
    public void setWellRated(Boolean wellRated) { this.wellRated = wellRated; }

    // Métodos de conveniencia
    public Double getDistanceMeters() {
        return totalDistance != null ? totalDistance.doubleValue() : null;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTime;
    }

    public String getDestinationName() {
        return toPlace != null ? toPlace.getName() : null;
    }

    public String getDestinationId() {
        return toPlace != null ? toPlace.getId() : null;
    }

    public boolean isPopularRoute() {
        return popular != null && popular;
    }

    public boolean isWellRatedRoute() {
        return wellRated != null && wellRated;
    }

    public boolean hasRating() {
        return averageRating != null && totalRatings != null && totalRatings > 0;
    }

    @Override
    public String toString() {
        return "RouteResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", destination=" + getDestinationName() +
                ", distance=" + formattedDistance +
                ", time=" + formattedTime +
                ", difficulty='" + difficulty + '\'' +
                ", rating=" + ratingText +
                '}';
    }
}