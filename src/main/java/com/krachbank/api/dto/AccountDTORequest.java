package com.krachbank.api.dto;

import org.iban4j.Iban;

import com.krachbank.api.models.AccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTORequest implements DTO {

    private Long id;
    private Iban iban;
    private AccountType accountType;
    private double balance;
    private double absoluteLimit;
    private String userId;


}
