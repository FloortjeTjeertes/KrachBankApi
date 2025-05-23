package com.krachbank.api.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Transaction implements Model{

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal amount;


    private LocalDateTime createdAt;

    @OneToOne
    private User initiator;

    @ManyToOne
    private Account fromAccount;

    @ManyToOne
    private Account toAccount;
    
     

    private String description;

}
