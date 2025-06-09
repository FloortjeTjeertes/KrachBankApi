package com.krachbank.api.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne; // Keep this import
// import jakarta.persistence.OneToOne; // You can remove this import if you don't use it elsewhere
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

    @ManyToOne // <--- CHANGE THIS FROM @OneToOne
    private User initiator; // This will map to initiator_id (or similar FK name) in the DB

    @ManyToOne
    private Account fromAccount;

    @ManyToOne
    private Account toAccount;

    private String description;

    public LocalDateTime getCreatedAt() {
        return createdAt == null ? LocalDateTime.now() : createdAt;
    }

}