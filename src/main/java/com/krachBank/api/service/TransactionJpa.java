package com.krachbank.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.repository.TransactionRepository;

@Service
public class TransactionJpa implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionJpa(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTransaction'");
    }

    @Override
    public Transaction getTransactionById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionById'");
    }

    @Override
    public Transaction getTransactionByFilter(TransactionFilter filter) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionByFilter'");
    }

    @Override
    public List<Transaction> getAllTransactions() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTransactions'");
    }

    @Override
    public Transaction updateTransaction(Long id, Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTransaction'");
    }

}
