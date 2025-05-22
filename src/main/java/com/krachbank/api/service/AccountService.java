package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.models.Account;

public interface AccountService extends Service<AccountDTO, Account> {
    public List<AccountDTO> getAccounts();

    public AccountDTO getAccountById(Long id);

    public Account createAccount(Account account);

    public List<Account> createAccounts(List<Account> accounts);

    public AccountDTO updateAccount(Long id, Account account);

    public void removeAccount(Account account);
}

