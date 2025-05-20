package com.krachbank.api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;

public interface TransactionService extends Service<TransactionDTO, Transaction> {

    public Optional<Transaction> createTransaction(TransactionDTO transaction);
    public Optional<Transaction> getTransactionById(Long id); 
    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter);
    public Boolean reachedAbsoluteLimit(Account account, BigDecimal amount) throws Exception;
    public Boolean reachedDailyTransferLimit(User  user, BigDecimal amount) throws Exception;
     Boolean transferAmountBiggerThenTransferLimit(Account account, BigDecimal amount) throws Exception;
    public List<Transaction> getTransactionsByFilter(TransactionFilter filter);
    public List<Transaction> getAllTransactions();
    public Optional<Transaction> updateTransaction(Long id, TransactionDTO transaction);
}