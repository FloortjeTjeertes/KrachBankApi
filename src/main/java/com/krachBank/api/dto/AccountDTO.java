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
public class AccountDTO implements DTO {

    private Long id;
    private Iban IBAN;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal absoluteLimit;
    private String userId;

   

}
