package com.krachbank.api.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.krachbank.api.dto.UserDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User implements Model {


    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private BigDecimal dailyLimit;

    private String password;

    private LocalDateTime createdAt;

    private boolean verified;

    private boolean active;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private int bsn;

   

 

}
