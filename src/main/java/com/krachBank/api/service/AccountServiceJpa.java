package com.krachbank.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.repository.AccountRepository;

@Service
public class AccountServiceJpa implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceJpa(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Transaction createAccount(Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public Transaction getAccountById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountById'");
    }

    @Override
    public Transaction getAccountByFilter(AccountFilter filter) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountByFilter'");
    }

    @Override
    public List<Transaction> getAllAccount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllAccount'");
    }

    @Override
    public Transaction updateAccount(Long id, Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAccount'");
    }


}
