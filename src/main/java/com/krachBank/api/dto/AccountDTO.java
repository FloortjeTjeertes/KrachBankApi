package com.krachBank.api.dto;

import java.math.BigDecimal;

import org.iban4j.Iban;

import com.krachBank.api.models.Account;
import com.krachBank.api.models.AccountType;
import com.krachBank.api.models.Model;

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
    private BigDecimal balance;
    private BigDecimal absoluteLimit;
    private String userId;

    @Override
    public Model ToModel() {
        return new Account();
    }

}
