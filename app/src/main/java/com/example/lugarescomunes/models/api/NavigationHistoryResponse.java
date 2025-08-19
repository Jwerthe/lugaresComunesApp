package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

public class NavigationHistoryResponse extends NavigationResponse {
    @SerializedName("route")
    private RouteResponse route;

    public RouteResponse getRoute() { return route; }
    public void setRoute(RouteResponse route) { this.route = route; }
}
