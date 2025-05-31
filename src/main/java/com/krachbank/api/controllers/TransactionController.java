package com.krachbank.api.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.TransactionService;

@RestController
@RequestMapping("/transactions")
public class TransactionController implements Controller<Transaction, TransactionDTOResponse> {

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

            return ResponseEntity.ok(transactionService.toDTO(transaction));

        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error.getMessage());
        }

    }

    @PostMapping
    public ResponseEntity<?> createTransaction(TransactionDTOResponse transactionDTO) {
        try {
            Transaction transaction = toModel(transactionDTO);
            Optional<Transaction> createdTransaction = transactionService.createTransaction(transaction);
            if (createdTransaction.isPresent()) {
                return ResponseEntity.ok(transactionService.toDTO(createdTransaction.get()));

            } else {
                throw new Exception("transaction did not safe right");
            }
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error.getMessage());
        }
    }

    //TODO: transfer tomodel behaviour to the Transaction Mapper
    @Override
    public Transaction toModel(TransactionDTOResponse dto) {

        User initUser = new User();
        initUser.setId(dto.getInitiator());

        Optional<Account> fromAccount = accountService.getAccountByIBAN(dto.getSender());

        Optional<Account> receivingAccount = accountService.getAccountByIBAN(dto.getReceiver());

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getAmount());
        transaction.setFromAccount(fromAccount.get());
        transaction.setToAccount(receivingAccount.get());
        transaction.setInitiator(initUser);
        transaction.setCreatedAt(dto.getCreatedAt());
        transaction.setDescription(dto.getDescription());
        return transaction;

    }
}
