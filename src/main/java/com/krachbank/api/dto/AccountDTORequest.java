package com.krachbank.api.dto;

import java.math.BigDecimal;

import org.iban4j.Iban;

import com.krachbank.api.models.AccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTORequest extends AccountDTO {

    private Long id;
    private Iban iban;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal absoluteLimit;
    private BigDecimal transactionLimit;
    private String userId;
    private boolean isActive;

}
