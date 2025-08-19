package com.example.lugarescomunes.models.api;

public class EmailValidationResponse {
    private boolean available;

    // Constructors
    public EmailValidationResponse() {}

    public EmailValidationResponse(boolean available) {
        this.available = available;
    }

    // Getters y setters
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}