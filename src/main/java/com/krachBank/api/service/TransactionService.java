package com.krachbank.api.service;

import java.util.List;
import java.util.Optional;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Transaction;

public interface TransactionService extends Service<TransactionDTO, Transaction> {

    public Optional<Transaction> createTransaction(Transaction transaction);
    public Optional<Transaction> getTransactionById(Long id); 
    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter);
    public List<Transaction> getTransactionsByFilter(TransactionFilter filter);
    public List<Transaction> getAllTransactions();
    public Optional<Transaction> updateTransaction(Long id, Transaction transaction);
}