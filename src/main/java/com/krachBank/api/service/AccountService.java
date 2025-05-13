package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.models.Transaction;

public interface AccountService {

    public Transaction createAccount(Transaction transaction);
    public Transaction getAccountById(Long id); 
    public Transaction getAccountByFilter(AccountFilter filter);
    public List<Transaction> getAllAccount();
    public Transaction updateAccount(Long id, Transaction transaction);
} 
