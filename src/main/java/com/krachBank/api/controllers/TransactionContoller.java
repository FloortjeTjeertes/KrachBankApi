package com.krachbank.api.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.ErrorDTO;
import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.service.TransactionService;

@RestController
@RequestMapping("/transactions")
public class TransactionContoller {

    private final TransactionService transactionService;

    public TransactionContoller(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @GetMapping
    public ResponseEntity<?> getTransactions(@ModelAttribute TransactionFilter filter) {
        try {
            List<Transaction> transaction = transactionService.getTransactionsByFilter(filter);
            if (transaction.isEmpty()) {
                return null;
            }
            
            return ResponseEntity.ok(transactionService.toDTO(transaction));

        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error.getMessage());
        }

    }

    @PostMapping
    public ResponseEntity<?> createTransaction(TransactionDTO transactionDTO) {
        try {

           
            Optional<Transaction> createdTransaction = transactionService.createTransaction(transactionDTO);
            if (createdTransaction.isPresent()) {
                return ResponseEntity.ok(transactionService.toDTO(createdTransaction.get()));

            } else {
                ErrorDTO error = new ErrorDTO("Transaction already exists", 400);
                return ResponseEntity.status(error.getCode()).body(error.getMessage());
            }
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error.getMessage());
        }
    }
 
}
