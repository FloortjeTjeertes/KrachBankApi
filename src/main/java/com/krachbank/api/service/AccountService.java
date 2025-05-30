package com.krachbank.api.service;

import java.util.List;
import java.util.Optional;

import org.iban4j.Iban;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.models.Account;

import jakarta.transaction.Transactional;

public interface AccountService extends Service<AccountDTO, Account> {
    public List<Account> getAccountsByFilter(AccountFilter filter);

    public Optional<Account> getAccountById(Long id) throws Exception;

    public Account createAccount(Account account);

    public List<Account> createAccounts(List<Account> accounts);

    @Transactional
    public Optional<Account> updateAccount(Long id, Account account) throws Exception;

    public void removeAccount(Account account);

    public Optional<Account> getAccountByIBAN(String iban);

    public List<Account> getAccountsByUserId(Long userId);


}
