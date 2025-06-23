package com.krachbank.api.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.iban4j.Iban;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;





class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    @Test
    void testSetAndGetId() {
        account.setId(1L);
        assertEquals(1L, account.getId());
    }

    @Test
    void testSetAndGetIban() {
        Iban iban = Iban.valueOf("DE89370400440532013000");
        account.setIban(iban);
        assertEquals(iban, account.getIban());
    }

    @Test
    void testSetAndGetBalance() {
        BigDecimal balance = new BigDecimal("100.50");
        account.setBalance(balance);
        assertEquals(balance, account.getBalance());
    }

    @Test
    void testSetAndGetAbsoluteLimit() {
        BigDecimal limit = new BigDecimal("500.00");
        account.setAbsoluteLimit(limit);
        assertEquals(limit, account.getAbsoluteLimit());
    }

    @Test
    void testSetAndGetAccountType() {
        AccountType type = AccountType.CHECKING;
        account.setAccountType(type);
        assertEquals(type, account.getAccountType());
    }

    @Test
    void testSetAndGetCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        account.setCreatedAt(now);
        assertEquals(now, account.getCreatedAt());
    }

    @Test
    void testSetAndGetUser() {
        User user = mock(User.class);
        account.setUser(user);
        assertEquals(user, account.getUser());
    }

    @Test
    void testSetAndGetVerifiedBy() {
        User verifier = mock(User.class);
        account.setVerifiedBy(verifier);
        assertEquals(verifier, account.getVerifiedBy());
    }

    @Test
    void testSetAndGetTransactionsFrom() {
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        List<Transaction> transactions = Arrays.asList(t1, t2);
        account.setTransactionsFrom(transactions);
        assertEquals(transactions, account.getTransactionsFrom());
    }

    @Test
    void testSetAndGetTransactionsTo() {
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        List<Transaction> transactions = Arrays.asList(t1, t2);
        account.setTransactionsTo(transactions);
        assertEquals(transactions, account.getTransactionsTo());
    }

    @Test
    void testSetAndGetTransactionLimit() {
        BigDecimal limit = new BigDecimal("1000.00");
        account.setTransactionLimit(limit);
        assertEquals(limit, account.getTransactionLimit());
    }

    @Test
    void testGetTransactionsCombinesFromAndTo() {
        Transaction from1 = mock(Transaction.class);
        Transaction from2 = mock(Transaction.class);
        Transaction to1 = mock(Transaction.class);

        account.setTransactionsFrom(Arrays.asList(from1, from2));
        account.setTransactionsTo(Collections.singletonList(to1));

        List<Transaction> all = account.getTransactions();
        assertTrue(all.contains(from1));
        assertTrue(all.contains(from2));
        assertTrue(all.contains(to1));
        assertEquals(3, all.size());
    }

    @Test
    void testGetTransactionsWithNullLists() {
        account.setTransactionsFrom(Collections.emptyList());
        account.setTransactionsTo(Collections.emptyList());
        List<Transaction> all = account.getTransactions();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }
}