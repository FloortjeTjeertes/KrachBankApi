package com.krachbank.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import com.krachBank.api.repository.AccountRepository;
import com.krachBank.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.krachBank.api.models.Account;
import com.krachBank.api.models.Transaction;
import com.krachBank.api.service.TransactionJpa;
import com.krachBank.api.filters.TransactionFilter;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;



    

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = new Account();
        fromAccount.setId(1L);
        toAccount = new Account();
        toAccount.setId(2L);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setAmount(100.0);
        transaction1.setDate(LocalDateTime.now().minusDays(1));
        transaction1.setFromAccount(fromAccount);
        transaction1.setToAccount(toAccount);

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAmount(200.0);
        transaction2.setDate(LocalDateTime.now());
        transaction2.setFromAccount(fromAccount);
        transaction2.setToAccount(toAccount);

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
    }

    @Test
    void testMakeTransactionsSpecification_BySenderId() {
        TransactionFilter filter = new TransactionFilter();
        filter.setSenderId(1L);

        List<Transaction> results = transactionRepository.findAll(
            TransactionJpa.MakeTransactionsSpecification(filter)
        );

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getFromAccount());
            assertNotNull(transaction.getFromAccount().getId());
            assertEquals(filter.getSenderId(), transaction.getFromAccount().getId());
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByReceiverId() {
        TransactionFilter filter = new TransactionFilter();
        filter.setReceiverId(2L);

        List<Transaction> results = transactionRepository.findAll(
            TransactionJpa.MakeTransactionsSpecification(filter)
        );

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getToAccount());
            assertNotNull(transaction.getToAccount().getId());
            assertEquals(filter.getReceiverId(), transaction.getToAccount().getId());
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByMinAmount() {
        TransactionFilter filter = new TransactionFilter();
        filter.setMinAmount(200.0);
        List<Transaction> results = transactionRepository.findAll(
            TransactionJpa.MakeTransactionsSpecification(filter)
        );
       
        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getAmount());
            assertEquals(filter.getMinAmount(), transaction.getAmount());
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByMaxAmount() {
        TransactionFilter filter = new TransactionFilter();
        filter.setMaxAmount(100.0);
        List<Transaction> results = transactionRepository.findAll(
            TransactionJpa.MakeTransactionsSpecification(filter)
        );
     
        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getAmount());
            assertEquals(filter.getMaxAmount(), transaction.getAmount());

        });
    }

    @Test
    void testMakeTransactionsSpecification_ByBeforeDate() {
        TransactionFilter filter = new TransactionFilter();
        filter.setBeforeDate(LocalDateTime.now());
        List<Transaction> results = transactionRepository.findAll(
            TransactionJpa.MakeTransactionsSpecification(filter)
        );
     

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getDate());
            assertEquals(filter.getBeforeDate(), transaction.getDate());
        });
    }

    @Test
    void testMakeTransactionsSpecification_ByAfterDate() {
        TransactionFilter filter = new TransactionFilter();
        filter.setAfterDate(LocalDateTime.now().minusDays(1));

        List<Transaction> results = transactionRepository.findAll(
            TransactionJpa.MakeTransactionsSpecification(filter)
        );

        assertNotNull(results);
        results.forEach(transaction -> {
            assertNotNull(transaction.getDate());
            assertEquals(filter.getAfterDate(), transaction.getDate());
        });
    }
}
