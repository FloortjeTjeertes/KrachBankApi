package com.krachbank.api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // Default token type for JWTs

    // Constructor to initialize the token
    public JwtResponse(String accessToken) {
        this.token = accessToken;
    }

}