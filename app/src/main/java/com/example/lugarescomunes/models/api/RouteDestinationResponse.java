package com.example.lugarescomunes.models.api;

public class RouteDestinationResponse {
    private String placeId;
    private String placeName;
    private Integer routeCount;

    // Getters y setters
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public Integer getRouteCount() { return routeCount; }
    public void setRouteCount(Integer routeCount) { this.routeCount = routeCount; }
}
