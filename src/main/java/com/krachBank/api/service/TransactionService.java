package com.krachBank.api.service;

import java.util.List;
import java.util.Optional;

import com.krachBank.api.filters.TransactionFilter;
import com.krachBank.api.models.Transaction;

public interface TransactionService {

    public Optional<Transaction> createTransaction(Transaction transaction);
    public Optional<Transaction> getTransactionById(Long id); 
    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter);
    public List<Transaction> getTransactionsByFilter(TransactionFilter filter);
    public List<Transaction> getAllTransactions();
    public Optional<Transaction> updateTransaction(Long id, Transaction transaction);
}