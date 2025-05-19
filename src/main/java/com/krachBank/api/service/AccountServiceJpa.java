package com.krachbank.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.dto.DTO;
import com.krachbank.api.models.Account;
import com.krachbank.api.repository.AccountRepository;


@Service
public class AccountServiceJpa implements AccountService {

    private final AccountRepository accountRepository;



    public AccountServiceJpa(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<AccountDTO> getAccounts() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccounts'");
    }

    @Override
    public AccountDTO getAccountById(Long id) {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountById'");
    }

    @Override
  public List<Account> createAccounts(List<Account> accounts) {
        // Validate accounts
        for (Account account : accounts) {
            if (account == null) {
                throw new IllegalArgumentException("Account cannot be null");
            }
            if (account.getIBAN() == null) {
                throw new IllegalArgumentException("Account number is required");
            }
            if (account.getBalance() == null) {
                throw new IllegalArgumentException("Account balance is required");
            }
            if (account.getUser() == null) {
                throw new IllegalArgumentException("Account owner is required");
            }
        }
        return accountRepository.saveAll(accounts);
    }

    @Override
    public Account createAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (account.getIBAN() == null) {
            throw new IllegalArgumentException("Account number is required");
        }
        if (account.getBalance() == null) {
            throw new IllegalArgumentException("Account balance is required");
        }
        if (account.getUser() == null) {
            throw new IllegalArgumentException("Account owner is required");
        }

        return accountRepository.save(account);
    }

    @Override
    public AccountDTO updateAccount(Long id, Account account) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAccount'");
    }

    @Override
    public void removeAccount(Account account) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAccount'");
    }

    @Override
    public Account toModel(AccountDTO dto) {

        return new Account();
    }

    @Override
    public AccountDTO toDTO(Account model) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toDTO'");
    }





}
