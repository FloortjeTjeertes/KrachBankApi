package com.krachbank.api.service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.iban4j.Iban;
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
    public AccountDTOResponse toDTO(Account model) {
        AccountDTOResponse accountDTO = new AccountDTOResponse();
        accountDTO.setType(model.getAccountType());
        accountDTO.setBalance(model.getBalance());
        accountDTO.setAbsoluteLimit(model.getAbsoluteLimit());
        accountDTO.setTransactionLimit(model.getTransactionLimit());
        accountDTO.setOwner(model.getUser().getId().toString());
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

    // add pagination filter to this method
    @Override
    public Optional<Account> getAccountByIBAN(String iban) {

        if (iban == null) {
            throw new IllegalArgumentException("IBAN cannot be null");
        }
        Iban ibanObj = Iban.valueOf(iban);
        if (ibanObj == null) {
            throw new IllegalArgumentException("IBAN is not valid");
        }
        Optional<Account> account = accountRepository.findByIban(ibanObj);
        if (!account.isPresent()) {
            throw new IllegalArgumentException("Account with this IBAN does not exist");
        }
        return account;
    }

    // add pagination filter to this method
    @Override
    public Page<Account> getAccountsByUserId(Long userId, AccountFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter cannot be null");
        }
        Specification<Account> specification = makeAccountFilterSpecification(filter);
        Pageable pageable = filter.toPageAble();
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Page<Account> accounts = accountRepository.findByUserId(userId, pageable);

        return accounts;
    }

    public static Specification<Account> makeAccountFilterSpecification(AccountFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicate = new ArrayList<>();

            if (filter != null) {
                if (filter.getIBAN() != null && !filter.getIBAN().isEmpty()) {
                    predicate.add(cb.equal(root.get("iban"), filter.getIBAN()));
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
            }
            return cb.and(predicate.toArray(new Predicate[0]));
        };
    }

    

}
