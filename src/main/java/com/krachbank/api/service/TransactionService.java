package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.BaseFilter;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;

import jakarta.transaction.Transactional;

public interface TransactionService extends Service<TransactionDTOResponse, Transaction> {

    @Transactional
    public Optional<Transaction> createTransaction(Transaction transaction,String UserName) throws Exception;

    public Optional<Transaction> getTransactionById(Long id) throws Exception;

    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter);

    public Page<Transaction> getTransactionsByFilter(TransactionFilter filter);

    public Page<Transaction> getAllTransactions(BaseFilter filter);

    @Transactional
    public Optional<Transaction> updateTransaction(Long id, Transaction transaction) throws Exception;

    public BigDecimal getUserTotalAmountSpendAtDate(User user, LocalDateTime date);
}