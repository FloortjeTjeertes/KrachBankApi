package com.krachbank.api.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.service.TransactionService;

@RestController
@RequestMapping("/transactions")
public class TransactionContoller {

    private final TransactionService transactionService;

    public TransactionContoller(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public TransactionDTO getTransactions(){

    }

}
