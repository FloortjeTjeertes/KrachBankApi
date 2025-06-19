package com.krachbank.api.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Transaction implements Model{

    @Id
    @GeneratedValue // Defaults to GenerationType.AUTO, which is often fine.
    // For explicit IDENTITY generation (DB handles ID, like H2, MySQL AUTO_INCREMENT)
    // you might use @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private LocalDateTime createdAt;

    @ManyToOne
    private User initiator;


    @ManyToOne
    @JsonBackReference
    private Account fromAccount;

    @ManyToOne
    @JsonBackReference
    private Account toAccount;

    private String description;

    public LocalDateTime getCreatedAt() {
        return createdAt == null ? LocalDateTime.now() : createdAt;
    }

}