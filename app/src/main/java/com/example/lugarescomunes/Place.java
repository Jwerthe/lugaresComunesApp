package com.example.lugarescomunes;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Place {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("what3words")
    private String what3words;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("is_available")
    private boolean isAvailable;

    @SerializedName("place_type")
    private String placeTypeString;

    @SerializedName("capacity")
    private int capacity;

    @SerializedName("schedule")
    private String schedule;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("building_name")
    private String buildingName;

    @SerializedName("floor_number")
    private Integer floorNumber;

    @SerializedName("room_code")
    private String roomCode;

    @SerializedName("equipment")
    private List<String> equipment;

    @SerializedName("accessibility_features")
    private List<String> accessibilityFeatures;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Campos calculados/locales (no en DB)
    private boolean isFavorite;
    private int distanceInMeters;
    private PlaceType type;

    // Constructor completo
    public Place(String id, String name, String category, String description,
                 String what3words, boolean isAvailable, int distanceInMeters, PlaceType type) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.what3words = what3words;
        this.isAvailable = isAvailable;
        this.distanceInMeters = distanceInMeters;
        this.type = type;
        this.placeTypeString = type != null ? type.name() : "CLASSROOM";
        this.isFavorite = false;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.imageUrl = "";
        this.schedule = "";
        this.capacity = 0;
    }

    // Constructor vacío
    public Place() {
        this.isFavorite = false;
        this.distanceInMeters = 0;
        this.type = PlaceType.CLASSROOM;
        this.placeTypeString = "CLASSROOM";
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getWhat3words() {
        return what3words;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getDistanceInMeters() {
        return distanceInMeters;
    }

    public PlaceType getType() {
        if (type == null && placeTypeString != null) {
            try {
                type = PlaceType.valueOf(placeTypeString);
            } catch (IllegalArgumentException e) {
                type = PlaceType.CLASSROOM;
            }
        }
        return type != null ? type : PlaceType.CLASSROOM;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSchedule() {
        return schedule;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public List<String> getAccessibilityFeatures() {
        return accessibilityFeatures;
    }

    public String getPlaceTypeString() {
        return placeTypeString;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWhat3words(String what3words) {
        this.what3words = what3words;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setDistanceInMeters(int distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }

    public void setType(PlaceType type) {
        this.type = type;
        this.placeTypeString = type != null ? type.name() : "CLASSROOM";
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setEquipment(List<String> equipment) {
        this.equipment = equipment;
    }

    public void setAccessibilityFeatures(List<String> accessibilityFeatures) {
        this.accessibilityFeatures = accessibilityFeatures;
    }

    public void setPlaceTypeString(String placeTypeString) {
        this.placeTypeString = placeTypeString;
        // Actualizar el enum también
        if (placeTypeString != null) {
            try {
                this.type = PlaceType.valueOf(placeTypeString);
            } catch (IllegalArgumentException e) {
                this.type = PlaceType.CLASSROOM;
            }
        }
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Métodos útiles
    public String getFormattedDistance() {
        if (distanceInMeters < 1000) {
            return distanceInMeters + "m";
        } else {
            double kilometers = distanceInMeters / 1000.0;
            return String.format("%.1fkm", kilometers);
        }
    }

    public String getAvailabilityText() {
        return isAvailable ? "Disponible" : "Ocupado";
    }

    public int getAvailabilityColor() {
        return isAvailable ? android.R.color.holo_green_light : android.R.color.holo_red_light;
    }

    public int getTypeIcon() {
        switch (getType()) {
            case CLASSROOM:
                return R.drawable.ic_classroom;
            case LABORATORY:
                return R.drawable.ic_laboratory;
            case LIBRARY:
                return R.drawable.ic_library;
            case CAFETERIA:
                return R.drawable.ic_cafeteria;
            case OFFICE:
                return R.drawable.ic_office;
            case AUDITORIUM:
                return R.drawable.ic_auditorium;
            case SERVICE:
                return R.drawable.ic_service;
            default:
                return R.drawable.ic_classroom;
        }
    }

    public int getCategoryColor() {
        switch (getType()) {
            case CLASSROOM:
                return android.R.color.holo_blue_light;
            case LABORATORY:
                return android.R.color.holo_orange_light;
            case LIBRARY:
                return android.R.color.holo_purple;
            case CAFETERIA:
                return android.R.color.holo_green_light;
            case OFFICE:
                return android.R.color.holo_red_light;
            case AUDITORIUM:
                return android.R.color.darker_gray;
            case SERVICE:
                return android.R.color.holo_blue_dark;
            default:
                return android.R.color.holo_blue_light;
        }
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", what3words='" + what3words + '\'' +
                ", isAvailable=" + isAvailable +
                ", distanceInMeters=" + distanceInMeters +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Place place = (Place) obj;
        return id != null ? id.equals(place.id) : place.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}