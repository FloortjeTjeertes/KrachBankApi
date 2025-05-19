package com.krachbank.api.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String transferLimit;
    private String createdAt;
    private boolean isVerified;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private int BSN;

    public UserDTO() {
        // Default constructor
    }

    public UserDTO(Long id, String transferLimit, String createdAt, boolean isVerified,
            boolean isActive, String firstName, String lastName, String email, String phoneNumber, int BSN) {
        this.id = id;
        this.transferLimit = transferLimit;
        this.createdAt = createdAt;
        this.isVerified = isVerified;
        this.isActive = isActive;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.BSN = BSN;
    }

}
