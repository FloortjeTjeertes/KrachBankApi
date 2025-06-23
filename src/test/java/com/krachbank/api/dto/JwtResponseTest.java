package com.krachbank.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JwtResponseTest {

    @Test
    void constructorShouldSetToken() {
        JwtResponse response = new JwtResponse("abc123");
        assertEquals("abc123", response.getToken());
    }

    @Test
    void defaultTypeShouldBeBearer() {
        JwtResponse response = new JwtResponse("token");
        assertEquals("Bearer", response.getType());
    }

    @Test
    void setTokenShouldUpdateToken() {
        JwtResponse response = new JwtResponse("oldToken");
        response.setToken("newToken");
        assertEquals("newToken", response.getToken());
    }

    @Test
    void setTypeShouldUpdateType() {
        JwtResponse response = new JwtResponse("token");
        response.setType("CustomType");
        assertEquals("CustomType", response.getType());
    }
}