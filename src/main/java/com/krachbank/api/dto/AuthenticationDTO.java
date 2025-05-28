// src/main/java/com/krachbank/api/dto/AuthenticationDTO.java
package com.krachbank.api.dto;

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
    private Double dailyLimit;
    private Double transferLimit;
    private LocalDateTime createdAt;
    private boolean isVerified;
    private boolean isActive;

    /**
     * Converts this AuthenticationDTO to a User model.
     * Note: This conversion will not include sensitive data like password.
     * It's primarily for mapping core identity details for updates or partial creation.
     * If creating a new user, password hashing would be handled in the service.
     *
     * @return A User model populated with data from this DTO.
     */
    public User ToModel() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPhoneNumber(this.phoneNumber);
        user.setBSN(this.BSN);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setDailyLimit(this.dailyLimit);
        user.setTransferLimit(this.transferLimit);
        user.setCreatedAt(this.createdAt);
        user.setVerified(this.isVerified);
        user.setActive(this.isActive);
        // Password is NOT set here. Handled by service.
        return user;
    }

    /**
     * Creates an AuthenticationDTO from a User model.
     * This method is suitable for exposing essential user identity and status
     * after authentication or for fetching profile details, without exposing
     * sensitive data like the hashed password.
     *
     * @param user The User model to convert.
     * @return An AuthenticationDTO populated from the User model.
     */
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
        dto.setTransferLimit(user.getTransferLimit());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setVerified(user.isVerified());
        dto.setActive(user.isActive());
        return dto;
    }
}