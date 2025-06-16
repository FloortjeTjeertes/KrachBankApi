package com.krachbank.api.mappers;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.krachbank.api.dto.TransactionDTORequest;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;

@Component
public class TransactionMapper extends BaseMapper<Transaction, TransactionDTORequest, TransactionDTOResponse> {

    private final AccountService accountService;

    public TransactionMapper(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public Transaction toModel(TransactionDTORequest dto) {

        User initUser = new User();
        initUser.setId(dto.getInitiator());

        Optional<Account> fromAccount = accountService.getAccountByIBAN(dto.getSender());

        Optional<Account> receivingAccount = accountService.getAccountByIBAN(dto.getReceiver());

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount.get());
        transaction.setToAccount(receivingAccount.get());
        transaction.setInitiator(initUser);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        return transaction;

    }

    @Override
    public TransactionDTOResponse toResponse(Transaction model) {
        TransactionDTOResponse response = new TransactionDTOResponse();
        response.setAmount(model.getAmount());
        response.setReceiver(model.getToAccount().getIban().toString());
        response.setSender(model.getFromAccount().getIban().toString());
        response.setDescription(model.getDescription());
        response.setInitiator(model.getInitiator().getId());
        response.setCreatedAt(model.getCreatedAt());

        return response;

    }

}
