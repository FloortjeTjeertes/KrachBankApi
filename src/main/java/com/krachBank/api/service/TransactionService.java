package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Transaction;

public interface TransactionService {

    public Transaction createTransaction(Transaction transaction);
    public Transaction getTransactionById(Long id); 
    public Transaction getTransactionByFilter(TransactionFilter filter);
    public List<Transaction> getAllTransactions();
    public Transaction updateTransaction(Long id, Transaction transaction);
}