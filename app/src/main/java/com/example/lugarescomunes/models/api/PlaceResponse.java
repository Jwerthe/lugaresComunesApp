package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.Set;

public class PlaceResponse {
    private String id;
    private String name;
    private String category;
    private String description;
    private String what3words;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @SerializedName("isAvailable")
    private Boolean isAvailable;

    @SerializedName("placeType")
    private String placeType;

    private Integer capacity;
    private String schedule;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("buildingName")
    private String buildingName;

    @SerializedName("floorNumber")
    private Integer floorNumber;

    @SerializedName("roomCode")
    private String roomCode;

    private Set<String> equipment;

    @SerializedName("accessibilityFeatures")
    private Set<String> accessibilityFeatures;

    @SerializedName("isRouteDestination")
    private Boolean isRouteDestination;

    @SerializedName("routeCount")
    private Integer routeCount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors
    public PlaceResponse() {}

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWhat3words() { return what3words; }
    public void setWhat3words(String what3words) { this.what3words = what3words; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public String getPlaceType() { return placeType; }
    public void setPlaceType(String placeType) { this.placeType = placeType; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public Set<String> getEquipment() { return equipment; }
    public void setEquipment(Set<String> equipment) { this.equipment = equipment; }

    public Set<String> getAccessibilityFeatures() { return accessibilityFeatures; }
    public void setAccessibilityFeatures(Set<String> accessibilityFeatures) { this.accessibilityFeatures = accessibilityFeatures; }

    public Boolean getIsRouteDestination() { return isRouteDestination; }
    public void setIsRouteDestination(Boolean isRouteDestination) { this.isRouteDestination = isRouteDestination; }

    public Integer getRouteCount() { return routeCount; }
    public void setRouteCount(Integer routeCount) { this.routeCount = routeCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}