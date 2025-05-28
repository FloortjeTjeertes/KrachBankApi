// src/main/java/com/krachbank/api/dto/AuthenticationResultDTO.java
package com.krachbank.api.dto;

import com.krachbank.api.models.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResultDTO implements DTO {

    private String token; // The JWT token
    private AuthenticationDTO userDetails; // The details of the authenticated user

    @Override
    public Model ToModel() {
        return null;
    }
}