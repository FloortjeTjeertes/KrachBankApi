package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.iban4j.Iban;

import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.User;

import jakarta.transaction.Transactional;

public interface AccountService extends Service<AccountDTO, Account> {
    public List<Account> getAccounts();

    public Optional<Account> getAccountById(Long id) throws Exception;

    public Account createAccount(Account account);

    public List<Account> createAccounts(List<Account> accounts);

    @Transactional
    public Optional<Account> updateAccount(Long id, Account account) throws Exception;

    public void removeAccount(Account account);

    public Optional<Account> getAccountByIBAN(Iban iban);

    public Boolean reachedAbsoluteLimit(Account account, BigDecimal amountToSubtract) throws Exception;

    public Boolean reachedDailyTransferLimit(User user, BigDecimal amount, LocalDateTime today) throws Exception;

    public Boolean transferAmountBiggerThenTransferLimit(Account account, BigDecimal amount) throws Exception;
}
