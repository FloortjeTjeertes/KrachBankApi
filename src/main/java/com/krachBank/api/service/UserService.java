package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;

public interface UserService {


    public Transaction createUser(User transaction);
    public Transaction getTransactionById(Long id); 
    public Transaction getTransactionByFilter(UserFilter filter);
    public List<Transaction> getAllUsers();
    public Transaction updateUser(Long id, Transaction transaction);
}
