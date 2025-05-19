package com.krachbank.api.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.iban4j.Iban;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.dto.DTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class Account implements Model {

    @Id
    @GeneratedValue
    private Long id;

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
        accountDTO.setBalance(this.balance);
        accountDTO.setAbsoluteLimit(this.absoluteLimit);
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
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getAbsoluteLimit() {
        return absoluteLimit;
    }

    public void setAbsoluteLimit(Double absoluteLimit) {
        this.absoluteLimit = absoluteLimit;
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
