package com.krachbank.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

@Service
public class UserServiceJpa implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceJpa(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Transaction getTransactionById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionById'");
    }

    @Override
    public Transaction getTransactionByFilter(UserFilter filter) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionByFilter'");
    }

    @Override
    public List<Transaction> getAllUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUsers'");
    }

    @Override
    public Transaction updateUser(Long id, Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public Transaction createUser(User transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }
   

}
