package com.krachbank.api.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Account implements Model {

    @Id
    private Long id;
    private String IBAN;
    private Double Balance;
    private Double AbsoluteLimit;
    private LocalDateTime createdAt;

    @OneToOne()
    private User user;
    @OneToOne()
    private User verifiedBy;

    @OneToMany(mappedBy = "fromAccount")
    private List<Transaction> transactionsFrom;

    @OneToMany(mappedBy = "toAccount")
    private List<Transaction> transactionsTo;


    public List<Transaction>  getTransactions(){
        List<Transaction> transactions = Stream.concat(this.transactionsFrom.stream(), this.transactionsTo.stream())
                .toList();
        return transactions;
    }


    @Override
    public DTO ToDTO() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ToDTO'");
    }

}
