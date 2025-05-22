package com.krachBank.api.service;

import java.util.List;

import com.krachBank.api.dto.AccountDTO;
import com.krachBank.api.models.Account;

public interface AccountService {
    public List<AccountDTO> getAccounts();

    public AccountDTO getAccountById(Long id);

    public Account createAccount(Account account);

    public List<Account> createAccounts(List<Account> accounts);

    public AccountDTO updateAccount(Long id, Account account);

    public void removeAccount(Account account);
}

