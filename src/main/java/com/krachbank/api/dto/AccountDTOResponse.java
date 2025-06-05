package com.krachbank.api.dto;

import java.math.BigDecimal;

import com.krachbank.api.models.AccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTOResponse extends AccountDTO {

    private String Iban;
    private BigDecimal balance;
    private long owner;
    private BigDecimal absoluteLimit;
    private BigDecimal transactionLimit;
    private AccountType type;
    private String createdAt;

}
