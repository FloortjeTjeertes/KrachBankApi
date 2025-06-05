package com.krachbank.api.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.TransactionDTORequest;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.TransactionService;


@RestController
@RequestMapping("/transactions")
public class TransactionController implements Controller<Transaction, TransactionDTOResponse, TransactionDTORequest> {

    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(@ModelAttribute TransactionFilter filter) {
        try {

            Page<Transaction> transactionPage = transactionService.getTransactionsByFilter(filter);
            List<Transaction> transaction =  transactionPage.getContent();
            if (transaction.isEmpty()) {
                return null;
            }
            List<TransactionDTOResponse> transactionDTOs = toResponseList(transaction);
            return ResponseEntity.ok(transactionDTOs);

        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error.getMessage());
        }

    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody TransactionDTORequest transactionDTO) {
        try {
            Transaction transaction = toModel(transactionDTO);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Transaction> createdTransaction = transactionService.createTransaction(transaction,username);


            if (createdTransaction.isPresent()) {
                return ResponseEntity.ok(toResponse(createdTransaction.get()));

            } else {
                throw new Exception("transaction did not safe right");
            }
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error.getMessage());
        }
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
        return transaction;

    }
    public List<TransactionDTOResponse> toResponseList(List<Transaction> models) {
        List<TransactionDTOResponse> dtos = new ArrayList<>();
        for (Transaction model : models) {
            dtos.add(toResponse(model));
        }
        return dtos;
    }
    public List<Transaction> toModelList(List<TransactionDTORequest> dtoList) {
        List<Transaction> accounts = new ArrayList<>();
        for (TransactionDTORequest dto : dtoList) {
            accounts.add(toModel(dto));
        }
        return accounts;
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
