// src/main/java/com/krachbank/api/dto/LoginRequest.java
package com.krachbank.api.dto;

import com.krachbank.api.models.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest implements DTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Override
    public Model ToModel() {
        return null;
    }
}