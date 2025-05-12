package com.krachbank.api.models;

import java.time.LocalDateTime;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "users")
public class User implements Model{
    
    @Id
    private Long id;

    private String name;

    private String dailyLimit;

    private String password;

    private LocalDateTime createdAt;

    private boolean verified;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private int bsn;

    public DTO ToDTO() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ToDTO'");
    }

}
