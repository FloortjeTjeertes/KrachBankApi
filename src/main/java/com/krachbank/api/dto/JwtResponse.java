package com.krachbank.api.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer"; // Default token type for JWTs

    // Constructor to initialize the token
    public JwtResponse(String accessToken) {
        this.token = accessToken;
    }

    // Getter for the token
    public String getToken() {
        return token;
    }

    // Setter for the token (optional)
    public void setToken(String token) {
        this.token = token;
    }

    // Getter for the token type
    public String getType() {
        return type;
    }

    // Setter for the token type (optional)
    public void setType(String type) {
        this.type = type;
    }
}