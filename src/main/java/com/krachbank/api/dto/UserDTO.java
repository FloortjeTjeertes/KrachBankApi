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
    private String username; // Added to carry username (e.g., first + last name, or email)

    // --- IMPORTANT NOTE ABOUT THIS CONSTRUCTOR ---
    // This constructor currently does not initialize any fields and takes 'int bsn' while
    // your field is 'String BSN'. If you intend to use a custom constructor, you MUST
    // initialize all fields, including 'password' and 'username', and ensure type consistency.
    // Otherwise, it's best to remove it and rely on Lombok's @AllArgsConstructor
    // and @NoArgsConstructor if they fit your needs.
    public UserDTO(Long id, String transferLimit, LocalDateTime createdAt, boolean verified, boolean active, String firstName, String lastName, String email, String phoneNumber, String bsn) {
        // Example:
        // this.id = id;
        // this.transferLimit = transferLimit;
        // this.createdAt = createdAt;
        // this.isVerified = verified;
        // this.isActive = active;
        // this.firstName = firstName;
        // this.lastName = lastName;
        // this.email = email;
        // this.phoneNumber = phoneNumber;
        // this.BSN = String.valueOf(bsn); // Correcting type mismatch
        // this.password = null; // Or pass as argument if needed
        // this.username = null; // Or pass as argument if needed
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
        user.setUsername(this.username); // Setting username on the model

        // IMPORTANT: The raw password from DTO should NOT be set directly on the User model
        // for persistence. Password encoding should happen in your UserService.createUser
        // implementation using BCryptPasswordEncoder before saving to the database.
        // user.setPassword(this.password); // <-- DO NOT DO THIS HERE DIRECTLY
        return user;
    }

    /**
     * Creates a UserDTO from a User model.
     * This method should typically NOT include the password for security reasons
     * when returning user data to the client.
     */
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