package com.krachbank.api.service;

import org.springframework.stereotype.Service;

import com.krachbank.api.repository.AccountRepository;

@Service
public class AccountServiceJpa implements AccountService {

    private final AccountRepository accountRepository;


}
