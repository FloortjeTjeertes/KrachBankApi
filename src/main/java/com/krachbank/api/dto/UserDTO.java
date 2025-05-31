// src/main/java/com/krachbank/api/dto/UserDTO.java
package com.krachbank.api.dto;

import java.math.BigDecimal;
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
    private BigDecimal transferLimit;
    private BigDecimal dailyLimit; // Assuming this is used for some purpose, otherwise can be removed
    private LocalDateTime createdAt;
    private boolean isVerified;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private int BSN; // Note: Ensure consistency with 'bsn' property in RegisterRequest/User entity if needed
    private String password; // Added for registration input
    private String username;


  
    
    public User ToModel() {
        User user = new User();
        user.setId(this.id);
        // Note: Check if transferLimit is always convertible to Double.
        // Consider handling potential NumberFormatException.
        user.setDailyLimit(this.transferLimit != null ? BigDecimal.ZERO: null);
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
  

}