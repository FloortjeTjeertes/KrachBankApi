package com.krachbank.api.service;

import org.springframework.stereotype.Service;

import com.krachbank.api.repository.TransactionRepository;

@Service
public class TransactionJpa implements TransactionService {

    private final TransactionRepository transactionRepository;

}
