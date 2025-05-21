package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;

public interface TransactionService extends Service<TransactionDTO, Transaction> {

    public Optional<Transaction> createTransaction(TransactionDTO transaction) throws Exception;
    public Optional<Transaction> getTransactionById(Long id); 
    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter);
    public Boolean reachedAbsoluteLimit(Account account, BigDecimal amountToSubtract) throws Exception;
    public Boolean reachedDailyTransferLimit(User  user, BigDecimal amount,LocalDateTime date) throws Exception;
     Boolean transferAmountBiggerThenTransferLimit(Account user, BigDecimal amount) throws Exception;
    public List<Transaction> getTransactionsByFilter(TransactionFilter filter);
    public List<Transaction> getAllTransactions();
    public Optional<Transaction> updateTransaction(Long id, TransactionDTO transaction);
}