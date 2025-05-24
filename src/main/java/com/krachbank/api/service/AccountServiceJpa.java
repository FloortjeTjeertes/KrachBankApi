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
        List<Account> accounts = new ArrayList<>();
        accountRepository.findAll().forEach(accounts::add);
        return accounts;
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
    @Transactional
    public List<Account> createAccounts(List<Account> accounts) {
        // Validate accounts
        for (Account account : accounts) {
            validateAccount(account);
        }
        return accountRepository.saveAll(accounts);
    }

    public Account validateAccount(Account account) {
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
        return account;

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
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (account.getId() == null || account.getId() <= 0) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (!accountRepository.existsById(account.getId())) {
            throw new IllegalArgumentException("Account does not exist");
        }
        accountRepository.delete(account);
    }

    // TODO: maybe make an parent base Service class that has generalised these
    // methods if posible
    @Override
    public AccountDTO toDTO(Account model) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setIBAN(model.getIBAN().toString());
        accountDTO.setType(model.getAccountType());
        accountDTO.setBalance(model.getBalance());
        accountDTO.setAbsoluteLimit(model.getAbsoluteLimit());
        accountDTO.setTransactionLimit(model.getTransactionLimit());
        accountDTO.setOwner(model.getUser().getId().toString());
        accountDTO.setCreatedAt(model.getCreatedAt().toString());
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
    public Optional<Account> getAccountByIBAN(String iban) {

        if (iban == null) {
            throw new IllegalArgumentException("IBAN cannot be null");
        }
        Iban ibanObj = Iban.valueOf(iban);
        if (ibanObj == null) {
            throw new IllegalArgumentException("IBAN is not valid");
        }
        Optional<Account> account = accountRepository.findByIBAN(ibanObj);
        if (!account.isPresent()) {
            throw new IllegalArgumentException("Account with this IBAN does not exist");
        }
        return account;
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        List<Account> accounts = accountRepository.findAll();
        List<Account> userAccounts = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getUser().getId().equals(userId)) {
                userAccounts.add(account);
            }
        }
        return userAccounts;
    }

}
