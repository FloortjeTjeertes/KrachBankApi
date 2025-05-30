package com.krachbank.api.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.iban4j.Iban;

import com.krachbank.api.converters.IbanConverter;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Account implements Model {

    @Id
    @GeneratedValue
    private Long id;
    @Convert(converter = IbanConverter.class)
    private Iban iban;
    private BigDecimal balance;
    private BigDecimal absoluteLimit;
    private AccountType accountType;
    private LocalDateTime createdAt;

    @ManyToOne
    private User user;
    @OneToOne()
    private User verifiedBy;

    @OneToMany(mappedBy = "fromAccount")
    private List<Transaction> transactionsFrom;

    @OneToMany(mappedBy = "toAccount")
    private List<Transaction> transactionsTo;

    private BigDecimal transactionLimit;


    public List<Transaction>  getTransactions(){
        List<Transaction> transactions = Stream.concat(this.transactionsFrom.stream(), this.transactionsTo.stream())
                .collect(java.util.stream.Collectors.toList());
        return transactions;
    }




}
