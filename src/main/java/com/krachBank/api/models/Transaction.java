package com.krachbank.api.models;

import java.time.LocalDateTime;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.Getter;

@Entity
@Data
public class Transaction implements Model{

    @Id
    private Long id;
    private Double amount;
    private LocalDateTime date;

    @OneToOne
    private User initiator;

    @ManyToOne
    private Account fromAccount;

    @ManyToOne
    private Account toAccount;
    

    @Override
    public DTO ToDTO() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ToDTO'");
    }

}
