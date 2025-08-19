package com.example.lugarescomunes.models.api;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    private String email;
    private String password;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("studentId")
    private String studentId;

    @SerializedName("userType")
    private String userType = "VISITOR"; // Por defecto VISITOR

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    // Getters y setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}