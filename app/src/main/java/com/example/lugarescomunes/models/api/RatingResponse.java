package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

public class RatingResponse {
    private String id;
    private Integer rating;
    private String comment;
    @SerializedName("createdAt")
    private String createdAt;

    // Constructors
    public RatingResponse() {}

    public RatingResponse(String id, Integer rating) {
        this.id = id;
        this.rating = rating;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}