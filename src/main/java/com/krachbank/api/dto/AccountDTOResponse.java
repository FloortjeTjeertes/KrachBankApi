package com.krachbank.api.dto;

import java.math.BigDecimal;

import com.krachbank.api.models.AccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTOResponse implements DTO {

    private String IBAN;
    private BigDecimal balance;
    private String owner;
    private BigDecimal absoluteLimit;
    private AccountType type;
    private BigDecimal transactionLimit;
    private String createdAt;

}
