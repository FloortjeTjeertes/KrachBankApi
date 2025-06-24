package com.krachbank.api.service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.iban4j.Iban;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.repository.AccountRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

@Service
public class AccountServiceJpa implements AccountService {

    private final AccountRepository accountRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountServiceJpa(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Page<Account> getAccountsByFilter(AccountFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null");
        }
        Specification<Account> specification = makeAccountFilterSpecification(filter);
        Pageable pageable = filter.toPageAble();
        Page<Account> accountPage = accountRepository.findAll(specification, pageable);
        return accountPage;
    }

    @Override
    public Optional<Account> getAccountById(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new InvalidParameterException("invalid id: ID cannot be null or zero/negative.");
        }

        Optional<Account> account = accountRepository.findById(id);

        if (!account.isPresent()) {
            throw new Exception("Account for this id does not exist.");
        }

        return account;
    }


    @Override
    @Transactional
    public List<Account> createAccounts(List<Account> accounts) {
        for (Account account : accounts) {
            validateAccount(account);
        }
        return accountRepository.saveAll(accounts);
    }

    public Account validateAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (account.getIban() == null) {
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
        if (account.getIban() == null) {
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
        // --- REMOVED THE OUTER TRY-CATCH BLOCK ---
        if (id == null || id <= 0) {
            throw new InvalidParameterException("Invalid ID: ID cannot be null or zero/negative.");
        }
        // Retrieve the existing account from the database
        Optional<Account> existingAccountOptional = accountRepository.findById(id);

        // --- CRITICAL CHANGE HERE: Check if the account DOES NOT exist ---
        if (existingAccountOptional.isEmpty()) { // This is the correct check
            throw new InvalidParameterException("The account for this ID does not exist."); // Throw if NOT found
        }

        Account existingAccount = existingAccountOptional.get();

        // Only update the fields that are intended to be changed.
        // For this scenario, you're updating 'transactionLimit'.
        // If 'account' object also contains 'balance' or 'absoluteLimit'
        // that you want to allow updating, you should add them here.
        if (account.getBalance() != null) {
            existingAccount.setBalance(account.getBalance());
        }
        if (account.getAbsoluteLimit() != null) {
            existingAccount.setAbsoluteLimit(account.getAbsoluteLimit());
        }
        if (account.getTransactionLimit() != null) {
            existingAccount.setTransactionLimit(account.getTransactionLimit());
        }
        // Add other fields you want to update from the 'account' parameter to 'existingAccount'

        // Save the updated existing account. Since 'existingAccount' is a managed entity
        // within a @Transactional context, changes will be persisted.
        Account savedAccount = accountRepository.save(existingAccount);

        return Optional.of(savedAccount);
    }


    @Override
    public void removeAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (account.getId() == null || account.getId() <= 0) {
            throw new IllegalArgumentException("Account ID is invalid");
        }
        if (!accountRepository.existsById(account.getId())) {
            throw new IllegalArgumentException("Account does not exist");
        }
        accountRepository.delete(account);
    }


    @Override
    public Optional<Account> getAccountByIBAN(String iban) {
        logger.debug("Attempting to retrieve account by IBAN: {}", iban);

        if (iban == null || iban.trim().isEmpty()) {
            throw new IllegalArgumentException("IBAN cannot be null or empty");
        }
        Iban ibanObj = Iban.valueOf(iban);

        Optional<Account> account = accountRepository.findByIban(ibanObj);
        if (!account.isPresent()) {
            logger.warn("Account with IBAN {} not found.", iban);
            throw new IllegalArgumentException("Account with this IBAN does not exist");
        }
        logger.debug("Account found for IBAN: {}", iban);
        return account;
    }

    @Override
    public Page<Account> getAccountsByUserId(Long userId, AccountFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null");
        }

        filter.setUserId(userId);
        Specification<Account> specification = makeAccountFilterSpecification(filter);
        Pageable pageable = filter.toPageAble();
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Page<Account> accounts = accountRepository.findAll( specification,pageable);

        return accounts;
    }


    public static Specification<Account> makeAccountFilterSpecification(AccountFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicate = new ArrayList<>();

            if (filter != null) {
                System.out.println("Filter: " + filter.getIban());
                if (filter.getIban() != null && !filter.getIban().isEmpty()) {
                    predicate.add(cb.equal(root.get("iban"), filter.getIban()));
                }

                if (filter.getAccountType() != null) {
                    predicate.add(cb.equal(root.get("accountType"), filter.getAccountType()));
                }
                if (filter.getBalanceMin() != null) {
                    predicate.add(cb.greaterThanOrEqualTo(root.get("balance"), filter.getBalanceMin()));
                }
                if (filter.getBalanceMax() != null) {
                    predicate.add(cb.lessThanOrEqualTo(root.get("balance"), filter.getBalanceMax()));
                }
                if (filter.getUserId() != null) {
                    predicate.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
                }

            }
            return cb.and(predicate.toArray(new Predicate[0]));
        };
    }

    @Override
    public AccountDTOResponse toDTO(Account model) {
        AccountDTOResponse accountDTO = new AccountDTOResponse();
        accountDTO.setType(model.getAccountType());
        accountDTO.setBalance(model.getBalance());
        accountDTO.setAbsoluteLimit(model.getAbsoluteLimit());
        accountDTO.setTransactionLimit(model.getTransactionLimit());
        accountDTO.setOwner(model.getUser().getId());
        accountDTO.setCreatedAt(model.getCreatedAt().toString());
        return accountDTO;
    }

    @Override
    public List<AccountDTOResponse> toDTO(List<Account> accounts) {
        List<AccountDTOResponse> accountDTOs = new ArrayList<>();
        for (Account accountDTO : accounts) {
            accountDTOs.add(toDTO(accountDTO));
        }
        return accountDTOs;
    }
}