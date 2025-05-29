package com.krachbank.api.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import com.krachbank.api.converters.IbanAttributeConverter;
import jakarta.persistence.*;
import org.iban4j.Iban;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.dto.DTO;

import lombok.Data;

@Entity
@Data
public class Account implements Model {

    @Id
    @GeneratedValue
    private Long id;
    @Convert(converter = IbanAttributeConverter.class)
    private Iban IBAN;
    private Double Balance;
    private Double AbsoluteLimit;
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


    public List<Transaction>  getTransactions(){
        List<Transaction> transactions = Stream.concat(this.transactionsFrom.stream(), this.transactionsTo.stream())
                .toList();
        return transactions;
    }


    @Override
    public AccountDTO toDTO() {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(this.id);
        accountDTO.setIBAN(this.IBAN);
        accountDTO.setAccountType(this.accountType);
        accountDTO.setBalance(this.getBalance());
        accountDTO.setAbsoluteLimit(this.getAbsoluteLimit());
        accountDTO.setUserId(this.user.getId().toString());
        return accountDTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Iban getIBAN() {
        return IBAN;
    }

    public void setIBAN(Iban iBAN) {
        IBAN = iBAN;
    }

    public Double getBalance() {
        return Balance;
    }

    public void setBalance(Double balance) {
        this.Balance = balance;
    }

    public Double getAbsoluteLimit() {
        return AbsoluteLimit;
    }

    public void setAbsoluteLimit(Double absoluteLimit) {
        this.AbsoluteLimit = absoluteLimit;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
