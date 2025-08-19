package com.example.lugarescomunes.models.api;

public class RateRouteRequest {
    private Integer rating; // 1-5
    private String comment;

    public RateRouteRequest(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    // Getters y setters
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
