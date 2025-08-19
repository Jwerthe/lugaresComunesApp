package com.example.lugarescomunes.models.api;

import java.util.List;

public class RouteDetailsResponse extends RouteResponse {
    private List<RoutePointResponse> points;

    public List<RoutePointResponse> getPoints() { return points; }
    public void setPoints(List<RoutePointResponse> points) { this.points = points; }
}
