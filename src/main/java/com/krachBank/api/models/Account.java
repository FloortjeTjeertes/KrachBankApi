package com.krachBank.api.models;

import com.krachBank.api.dto.AccountDTO;
import jakarta.persistence.*;
import lombok.Data;
import org.iban4j.Iban;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import java.math.BigDecimal;

@Entity
@Data
public class Account implements Model {

    @Id
    @GeneratedValue
    private Long id;

    private Iban IBAN;
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
    private BigDecimal balance;
    private BigDecimal absoluteLimit;

    public List<Transaction> getTransactions() {
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        this.balance = balance;
    }

    public BigDecimal getAbsoluteLimit() {
        return absoluteLimit;
    }

    public void setAbsoluteLimit(BigDecimal absoluteLimit) {
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
