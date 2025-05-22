package com.krachbank.api.service;

import org.iban4j.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krachbank.api.configuration.IBANGenerator;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class AccountServiceJpaTest {
    private AccountServiceJpa accountService;
    private AccountRepository accountRepository;
    private TransactionService transactionService;
    private IBANGenerator ibanGenerator;

    private User user1;
    private User user2;
    private Iban iban1;
    private Iban iban2;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        ibanGenerator = mock(IBANGenerator.class);
        transactionService = mock(TransactionService.class);
        accountService = new AccountServiceJpa(accountRepository, transactionService);

        user1 = new User();
        user1.setId(1L);
        user2 = new User();
        user2.setId(2L);

        iban1 = Iban.valueOf("NL91ABNA0417164300");
        iban2 = Iban.valueOf("NL64ABNA0417164301");

        account1 = new Account();
        account1.setId(1L);
        account1.setIBAN(iban1);
        account1.setBalance(BigDecimal.valueOf(1000.0));
        account1.setAbsoluteLimit(BigDecimal.valueOf(100.0));
        account1.setAccountType(AccountType.SAVINGS);
        account1.setUser(user1);

        account2 = new Account();
        account2.setId(2L);
        account2.setIBAN(iban2);
        account2.setBalance(BigDecimal.valueOf( 1500.0));
        account2.setAbsoluteLimit(BigDecimal.valueOf( 200.0));
        account2.setAccountType(AccountType.CHECKING);
        account2.setUser(user2);
    }

    @Test
    void testCreateAccount() {
        // Change values for this test
        account1.setBalance(BigDecimal.valueOf(1000.0));
        account1.setAbsoluteLimit(BigDecimal.valueOf(100.0));
        account1.setAccountType(AccountType.SAVINGS);
        account1.setUser(user1);
        account1.setIBAN(iban1);

        when(accountRepository.save(any(Account.class))).thenReturn(account1);

        Account savedAccount = accountService.createAccount(account1);

        assertNotNull(savedAccount);
        assertEquals(1L, savedAccount.getId());
        assertEquals(iban1, savedAccount.getIBAN());
        assertEquals(BigDecimal.valueOf(1000.0), savedAccount.getBalance());
        assertEquals(BigDecimal.valueOf(100.0), savedAccount.getAbsoluteLimit());
        assertEquals(AccountType.SAVINGS, savedAccount.getAccountType());
        assertEquals(user1, savedAccount.getUser());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccounts() {
        // Change values for this test
        account1.setBalance(BigDecimal.valueOf(500.0));
        account1.setAbsoluteLimit(BigDecimal.valueOf(50.0));
        account1.setAccountType(AccountType.SAVINGS);
        account1.setUser(user1);
        account1.setIBAN(iban1);

        account2.setBalance(BigDecimal.valueOf(1500.0));
        account2.setAbsoluteLimit(BigDecimal.valueOf(200.0));
        account2.setAccountType(AccountType.CHECKING);
        account2.setUser(user2);
        account2.setIBAN(iban2);

        List<Account> accounts = Arrays.asList(account1, account2);

        when(accountRepository.saveAll(anyList())).thenReturn(accounts);

        List<Account> savedAccounts = accountService.createAccounts(accounts);

        assertNotNull(savedAccounts);
        assertEquals(2, savedAccounts.size());
        assertEquals(account1, savedAccounts.get(0));
        assertEquals(account2, savedAccounts.get(1));
        verify(accountRepository, times(1)).saveAll(savedAccounts);
    }

    @Test
    void testCreateAccount_NullAccount_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(null));
    }

    @Test
    void testCreateAccount_NullIBAN_ThrowsException() {
        account1.setIBAN(null);
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account1));
    }

    @Test
    void testCreateAccount_NullBalance_ThrowsException() {
        account1.setBalance(null);
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account1));
    }

    @Test
    void testCreateAccount_NullUser_ThrowsException() {
        account1.setUser(null);
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(account1));
    }

    @Test
    void testCreateAccounts_EmptyList() {
        List<Account> emptyList = Arrays.asList();
        List<Account> result = accountService.createAccounts(emptyList);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateAccounts_NullList() {
        assertThrows(NullPointerException.class, () -> accountService.createAccounts(null));
    }
}
