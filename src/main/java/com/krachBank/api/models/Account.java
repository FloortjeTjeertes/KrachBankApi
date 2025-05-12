package com.krachbank.api.models;

import java.time.LocalDateTime;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Account implements Model {

    @Id
    private Long id;
    private String IBAN;
    private Double Balance;
    private Double AbsoluteLimit;
    private LocalDateTime CreatedAt;
     
 
    
    @Override
    public DTO ToDTO() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ToDTO'");
    }

}
