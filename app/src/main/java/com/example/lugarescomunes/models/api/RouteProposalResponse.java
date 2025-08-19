package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

public class RouteProposalResponse {
    private String id;
    private String name;
    private String description;
    private String status; // PENDING, APPROVED, REJECTED
    @SerializedName("destinationPlace")
    private PlaceResponse destinationPlace;
    @SerializedName("submittedBy")
    private UserResponse submittedBy;
    @SerializedName("adminNotes")
    private String adminNotes;
    @SerializedName("createdAt")
    private String createdAt;

    // Constructors
    public RouteProposalResponse() {}

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PlaceResponse getDestinationPlace() {
        return destinationPlace;
    }

    public void setDestinationPlace(PlaceResponse destinationPlace) {
        this.destinationPlace = destinationPlace;
    }

    public UserResponse getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(UserResponse submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}