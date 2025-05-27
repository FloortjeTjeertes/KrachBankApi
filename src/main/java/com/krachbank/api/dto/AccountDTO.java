package com.krachbank.api.dto;

import org.iban4j.Iban;

import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO implements DTO {

    private Long id;
    private Iban IBAN;
    private AccountType accountType;
    private double balance;
    private double absoluteLimit;
    private String userId;

    @Override
    public Model ToModel() {
        return new Account();
    }

}
