package com.krachbank.api.models;

import java.time.LocalDateTime;
import java.util.List;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

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

    @OneToMany(mappedBy = "user")
    private List<Account> accounts;

    public DTO ToDTO() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ToDTO'");
    }

}
