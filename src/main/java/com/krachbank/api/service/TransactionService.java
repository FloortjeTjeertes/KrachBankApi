package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;

import jakarta.transaction.Transactional;

public interface TransactionService extends Service<TransactionDTO, Transaction> {

    @Transactional
    public Optional<Transaction> createTransaction(TransactionDTO transaction) throws Exception;

    public Optional<Transaction> getTransactionById(Long id) throws Exception;

    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter);

    public List<Transaction> getTransactionsByFilter(TransactionFilter filter);

    public List<Transaction> getAllTransactions();

    @Transactional
    public Optional<Transaction> updateTransaction(Long id, TransactionDTO transaction);

    public BigDecimal getUserTotalAmountSpendAtDate(User user, LocalDateTime date);
}