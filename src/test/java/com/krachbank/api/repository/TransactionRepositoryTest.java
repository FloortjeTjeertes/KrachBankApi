package com.krachbank.api.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.iban4j.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.service.TransactionServiceJpa;
import com.krachbank.api.filters.TransactionFilter;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account fromAccount;
    private Account toAccount;
    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        fromAccount = new Account();
        toAccount = new Account();
        fromAccount.setIban(Iban.valueOf("NL91ABNA0417164300"));
        toAccount.setIban(Iban.valueOf("NL64ABNA0417164301"));

        fromAccount = accountRepository.save(fromAccount);
        toAccount = accountRepository.save(toAccount);

        transaction1 = new Transaction();
        transaction1.setAmount(BigDecimal.valueOf(100.0));
        transaction1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 1, 1));
        transaction1.setFromAccount(fromAccount);
        transaction1.setToAccount(toAccount);

        transaction2 = new Transaction();
        transaction2.setAmount(BigDecimal.valueOf(200.0));
        transaction2.setCreatedAt(LocalDateTime.of(2025, 1, 2, 1, 1));
        transaction2.setFromAccount(fromAccount);
        transaction2.setToAccount(toAccount);

        transaction1 = transactionRepository.save(transaction1);
        transaction2 = transactionRepository.save(transaction2);
    }

    @Test
    void testMakeTransactionsSpecification_BySenderIban() {
        TransactionFilter filter = new TransactionFilter();
        filter.setSenderIban(fromAccount.getIban().toString());

        List<Transaction> results = transactionRepository.findAll(
                TransactionServiceJpa.MakeTransactionsSpecification(filter));

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getFromAccount());
            assertNotNull(transaction.getFromAccount().getIban());
            assertEquals(filter.getSenderIban(), transaction.getFromAccount().getIban().toString());
        });
    }

    @Test
    void testMakeTransactionsSpecification_SenderIbanNotFound() {
        TransactionFilter filter = new TransactionFilter();
        filter.setSenderIban("NON_EXISTENT_IBAN"); // Non-existent sender IBAN

        List<Transaction> results = transactionRepository.findAll(
                TransactionServiceJpa.MakeTransactionsSpecification(filter));

        assertNotNull(results, "Results should not be null even if no transactions are found");
        assertEquals(0, results.size(), "Expected no transactions to be found for a non-existent sender IBAN");
    }

    @Test
    void testMakeTransactionsSpecification_ByReceiverIban() {
        TransactionFilter filter = new TransactionFilter();
        filter.setReceiverIban(toAccount.getIban().toString());

        List<Transaction> results = transactionRepository.findAll(
                TransactionServiceJpa.MakeTransactionsSpecification(filter));

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getToAccount());
            assertNotNull(transaction.getToAccount().getIban());
            assertEquals(filter.getReceiverIban(), transaction.getToAccount().getIban().toString());
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByMinAmount() {
        TransactionFilter filter = new TransactionFilter();
        filter.setMinAmount(BigDecimal.valueOf(100.0));
        List<Transaction> results = transactionRepository.findAll(
                TransactionServiceJpa.MakeTransactionsSpecification(filter));

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getAmount());
            assertTrue(transaction.getAmount().compareTo(filter.getMinAmount()) >= 0);
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByMaxAmount() {
        TransactionFilter filter = new TransactionFilter();
        filter.setMaxAmount(BigDecimal.valueOf(100.0));
        List<Transaction> results = transactionRepository.findAll(
                TransactionServiceJpa.MakeTransactionsSpecification(filter));

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getAmount());
            assertTrue(transaction.getAmount().compareTo(filter.getMaxAmount()) <= 0);
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByBeforeDate() {
        TransactionFilter filter = new TransactionFilter();
        filter.setBeforeDate(LocalDateTime.of(2025, 1, 2, 1, 1).toString());
        List<Transaction> results = transactionRepository.findAll(
                TransactionServiceJpa.MakeTransactionsSpecification(filter));
        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getCreatedAt());
            assertTrue(transaction.getCreatedAt().isBefore(filter.getBeforeDate()));
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByAfterDate() {
        TransactionFilter filter = new TransactionFilter();
        filter.setAfterDate(LocalDateTime.of(2025, 1, 1, 1, 1).toString());

        List<Transaction> results = transactionRepository.findAll(
                TransactionServiceJpa.MakeTransactionsSpecification(filter));

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getCreatedAt());
            assertTrue(transaction.getCreatedAt().isAfter(filter.getAfterDate()));
        });
    }

}
