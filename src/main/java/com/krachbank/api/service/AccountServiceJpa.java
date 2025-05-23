package com.krachbank.api.service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.iban4j.Iban;
import org.springframework.stereotype.Service;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.models.Account;
import com.krachbank.api.repository.AccountRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountServiceJpa implements AccountService {

    private final AccountRepository accountRepository;


    public AccountServiceJpa(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> getAccounts() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccounts'");
    }

    @Override
    public Optional<Account> getAccountById(Long id) throws Exception {

        if (id <= 0 || id == null) {
            throw new InvalidParameterException("invalid id");
        }

        Optional<Account> account = accountRepository.findById(id);

        // TODO: this could be moved to the getAccountById method
        if (!account.isPresent()) {
            throw new Exception("there is no account for this id");
        }

        return account;

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
    @Transactional
    public Optional<Account> updateAccount(Long id, Account account) throws Exception {

        try {
            if (id <= 0 || id == null) {
                throw new InvalidParameterException("invalid id");
            }
            if (account == null || account.getId() == null || account.getId() <= 0) {
                throw new InvalidParameterException("invalid account");
            }

            if (accountRepository.existsById(id)) {
                throw new InvalidParameterException("the account for this id does not exist");
            }

            // make sure that the account has the same id
            account.setId(id);

            accountRepository.save(account);

            Optional<Account> updatedAccount = getAccountById(id);

            return updatedAccount;
        } catch (Exception e) {
            throw new Exception("something went wrong while updating change has been reversed");
        }

    }

    @Override
    public void removeAccount(Account account) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAccount'");
    }

 

    //TODO: maybe make an parent base Service class that has generalised these methods if posible
    @Override
    public AccountDTO toDTO(Account model) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(model.getId());
        accountDTO.setIBAN(model.getIBAN());
        accountDTO.setAccountType(model.getAccountType());
        accountDTO.setBalance(model.getBalance());
        accountDTO.setAbsoluteLimit(model.getAbsoluteLimit());
        accountDTO.setAbsoluteLimit(model.getAbsoluteLimit());
        accountDTO.setUserId(model.getUser().getId().toString());
        return accountDTO;
    }

    @Override
    public List<AccountDTO> toDTO(List<Account> accounts) {
        List<AccountDTO> accountDTOs = new ArrayList<>();
        for (Account accountDTO : accounts) {
            accountDTOs.add(toDTO(accountDTO));
        }
        return accountDTOs;
    }

    @Override
    public Optional<Account> getAccountByIBAN(Iban iban) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountByIBAN'");
    }

   

}
