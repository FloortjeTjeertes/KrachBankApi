// src/main/java/com/krachbank/api/dto/UserDTO.java
package com.krachbank.api.dto;

import java.time.LocalDateTime;

import com.krachbank.api.models.User; // Assuming your User entity

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
public class UserDTO implements DTO {

    private Long id;
    private String transferLimit;
    private LocalDateTime createdAt;
    private boolean isVerified;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String BSN; // Note: Ensure consistency with 'bsn' property in RegisterRequest/User entity if needed
    private String password; // Added for registration input
    private String username;


    public UserDTO(Long id, String transferLimit, LocalDateTime createdAt, boolean verified, boolean active, String firstName, String lastName, String email, String phoneNumber, String bsn) {
    }

    @Override
    public User ToModel() {
        User user = new User();
        user.setId(this.id);
        // Note: Check if transferLimit is always convertible to Double.
        // Consider handling potential NumberFormatException.
        user.setDailyLimit(this.transferLimit != null ? Double.valueOf(this.transferLimit) : null);
        user.setCreatedAt(this.createdAt);
        user.setVerified(this.isVerified);
        user.setActive(this.isActive);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setPhoneNumber(this.phoneNumber);
        user.setBSN(this.BSN);
        user.setUsername(this.username);
        return user;
    }
    public static UserDTO fromModel(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setTransferLimit(user.getDailyLimit() != null ? String.valueOf(user.getDailyLimit()) : null);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setVerified(user.isVerified());
        dto.setActive(user.isActive());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setBSN(user.getBSN());
        dto.setUsername(user.getUsername()); // Setting username on the DTO from model
        // dto.setPassword(null); // Explicitly set password to null or omit from fromModel
        return dto;
    }

}