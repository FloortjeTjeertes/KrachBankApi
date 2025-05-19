package com.krachbank.api.service;

import java.util.List;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.models.Account;

public interface AccountService {
    public List<AccountDTO> getAccounts();

    public AccountDTO getAccountById(Long id);

    public AccountDTO createAccount(Account account);

    public List<AccountDTO> createAccounts(List<Account> accounts);

    public AccountDTO updateAccount(Long id, Account account);

    public void removeAccount(Account account);
}
