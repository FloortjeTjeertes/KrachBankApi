package com.krachbank.api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.krachbank.api.configuration.IBANGenerator;
import com.krachbank.api.dto.AccountDTORequest;
import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.models.Account;

@Component
public  class AccountMapper extends BaseMapper<Account, AccountDTORequest, AccountDTOResponse> {

    @Override
    public  Account toModel(AccountDTORequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("AccountDTOResponse cannot be null");
        }
        Account account = new Account();
        account.setIban(IBANGenerator.generateIBAN());
        account.setAccountType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setAbsoluteLimit(dto.getAbsoluteLimit());
        account.setTransactionLimit(dto.getTransactionLimit());
        account.setCreatedAt(LocalDateTime.now());

        return account;
    }

    @Override
    public AccountDTOResponse toResponse(Account model) {
        if (model == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        AccountDTOResponse dto = new AccountDTOResponse();
        dto.setIban(model.getIban().toString());
        dto.setBalance(model.getBalance());
        dto.setOwner(model.getUser().getId());
        dto.setAbsoluteLimit(model.getAbsoluteLimit());
        dto.setTransactionLimit(model.getTransactionLimit());
        dto.setType(model.getAccountType());
        dto.setCreatedAt(model.getCreatedAt() != null ? model.getCreatedAt().toString() : "Unknown");

        return dto;
    }

}
