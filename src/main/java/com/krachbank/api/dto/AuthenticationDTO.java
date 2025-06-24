// src/main/java/com/krachbank/api/dto/AuthenticationDTO.java
package com.krachbank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.krachbank.api.models.User; // Import the User model

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationDTO implements DTO {

    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String BSN;
    private String firstName;
    private String lastName;
    private BigDecimal dailyLimit;
    private LocalDateTime createdAt;
    private boolean isVerified;
    private boolean isActive;
    private boolean isAdmin;
    //static because it's a factory style constructor, and it does not depend on any existing dto
    public static AuthenticationDTO fromModel(User user) {
        AuthenticationDTO dto = new AuthenticationDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setBSN(user.getBSN());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDailyLimit(user.getDailyLimit());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setVerified(user.isVerified());
        dto.setActive(user.isActive());
        dto.setAdmin(user.isAdmin());
        return dto;
    }
}