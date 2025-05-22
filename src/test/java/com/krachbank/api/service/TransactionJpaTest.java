package com.krachbank.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.iban4j.Iban;
import org.iban4j.Iban4jException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.TransactionRepository;

public class TransactionJpaTest {

    TransactionJpa transactionService;
    AccountServiceJpa accountService;
    TransactionRepository transactionRepository;


    Transaction fullTransaction;
    Transaction fullTransaction2;
    List<Transaction> transactions;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        accountService = mock(AccountServiceJpa.class);
        transactionService = new TransactionJpa(transactionRepository, accountService);

        fullTransaction = new Transaction();
        fullTransaction.setId(1L);
        fullTransaction.setAmount(new BigDecimal("100.00"));
        fullTransaction.setDescription("Test transaction");
        fullTransaction.setCreatedAt(LocalDateTime.now());

        Iban iban = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        Account fromAccount = new Account();
        fromAccount.setId(10L);
        fromAccount.setIBAN(iban);
        fullTransaction.setFromAccount(fromAccount);

        Account toAccount = new Account();
        toAccount.setIBAN(iban2);
        toAccount.setId(20L);
        fullTransaction.setToAccount(toAccount);

        User initiator = new User();
        initiator.setId(100L);
        fullTransaction.setInitiator(initiator);

        fullTransaction2 = new Transaction();
        fullTransaction2.setId(2L);
        fullTransaction2.setAmount(new BigDecimal("250.50"));
        fullTransaction2.setDescription("Second test transaction");
        fullTransaction2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Account fromAccount2 = new Account();
        fromAccount2.setId(30L);
        fromAccount2.setIBAN(Iban.valueOf("DE44500105175407324931"));
        fullTransaction2.setFromAccount(fromAccount2);

        Account toAccount2 = new Account();
        toAccount2.setId(40L);
        toAccount2.setIBAN(Iban.valueOf("DE12500105170648489890"));
        fullTransaction2.setToAccount(toAccount2);

        User initiator2 = new User();
        initiator2.setId(200L);
        fullTransaction2.setInitiator(initiator2);

        transactions = List.of(fullTransaction, fullTransaction2);

        when(accountService.getAccountByIBAN(fullTransaction.getFromAccount().getIBAN()))
                .thenReturn(fullTransaction.getFromAccount());
        when(accountService.getAccountByIBAN(fullTransaction.getToAccount().getIBAN()))
                .thenReturn(fullTransaction.getToAccount());
        when(accountService.getAccountByIBAN(fullTransaction2.getFromAccount().getIBAN()))
                .thenReturn(fullTransaction2.getFromAccount());
        when(accountService.getAccountByIBAN(fullTransaction2.getToAccount().getIBAN()))
                .thenReturn(fullTransaction2.getToAccount());
    }





    @Test
    void testIsValidTransactionWithValidTransaction() {
        boolean result = transactionService.isValidTransaction(fullTransaction);

        assertEquals(true, result);
    }

    @Test
    void testIsValidTransactionWithZeroAmount() {
        Transaction transaction = fullTransaction;
        transaction.setAmount(BigDecimal.valueOf(0));

        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithNegativeAmount() {
        Transaction transaction = fullTransaction;
        transaction.setAmount(BigDecimal.valueOf(-1000));

        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithNullFromAccount() {
        Transaction transaction = fullTransaction;
        transaction.setFromAccount(null);
        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithNullToAccount() {
        Transaction transaction = fullTransaction;
        transaction.setToAccount(null);

        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithSameAccounts() {
        Transaction transaction = fullTransaction;

        Account account = new Account();
        account.setId(1L);
        transaction.setFromAccount(account);
        transaction.setToAccount(account);

        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testToModelWithValidDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(fullTransaction.getAmount());
        dto.setCreatedAt(fullTransaction.getCreatedAt());
        dto.setInitiator(fullTransaction.getInitiator().getId());
        dto.setSender(fullTransaction.getFromAccount().getIBAN().toString());
        dto.setReceiver(fullTransaction.getToAccount().getIBAN().toString());
        dto.setDescription(fullTransaction.getDescription());

        Transaction transaction = transactionService.toModel(dto);

        assertNotNull(transaction);
        assertEquals(fullTransaction.getAmount(), transaction.getAmount());
        assertEquals(fullTransaction.getCreatedAt(), transaction.getCreatedAt());
        assertEquals(fullTransaction.getDescription(), transaction.getDescription());
        assertNotNull(transaction.getInitiator());
        assertEquals(fullTransaction.getInitiator().getId(), transaction.getInitiator().getId());
        assertNotNull(transaction.getFromAccount());
        assertEquals(fullTransaction.getFromAccount().getIBAN(), transaction.getFromAccount().getIBAN());
        assertNotNull(transaction.getToAccount());
        assertEquals(fullTransaction.getToAccount().getIBAN(), transaction.getToAccount().getIBAN());
    }

    //TODO: maybe rename this test?
    @Test
    void testToModelWithNullFields() {
        TransactionDTO dto = new TransactionDTO();
        // Only set initiator from setup
        assertThrows(Iban4jException.class, ()-> transactionService.toModel(dto));
      
    }

    @Test
    void testToModelWithNullDTO() {
        assertThrows(NullPointerException.class, () -> transactionService.toModel(null));
    }

    @Test
    void testToDTOWithFullTransaction() {

        TransactionDTO dto = transactionService.toDTO(fullTransaction);

        assertNotNull(dto);
        assertEquals(fullTransaction.getAmount(), dto.getAmount());
        assertEquals(fullTransaction.getDescription(), dto.getDescription());
        assertEquals(fullTransaction.getCreatedAt().toString(), dto.getCreatedAt().toString());
        assertEquals(fullTransaction.getFromAccount().getIBAN().toString(), dto.getSender());
        assertEquals(fullTransaction.getToAccount().getIBAN().toString(), dto.getReceiver());
        assertEquals(fullTransaction.getInitiator().getId(), dto.getInitiator());
    }

    @Test
    void testToDTOWithNullTransaction() {
        assertThrows(NullPointerException.class, () -> transactionService.toDTO((Transaction) null));
    }

    @Test
    void testToDTOListWithMultipleTransactions() {

        List<TransactionDTO> dtos = transactionService.toDTO(transactions);

        assertNotNull(dtos);
        assertEquals(2, dtos.size());

        TransactionDTO dto1 = dtos.get(0);
        assertEquals(fullTransaction.getAmount(), dto1.getAmount());
        assertEquals(fullTransaction.getDescription(), dto1.getDescription());
        assertEquals(fullTransaction.getCreatedAt(), dto1.getCreatedAt());
        assertEquals(fullTransaction.getFromAccount().getIBAN().toString(), dto1.getSender());
        assertEquals(fullTransaction.getToAccount().getIBAN().toString(), dto1.getReceiver());
        assertEquals(fullTransaction.getInitiator().getId(), dto1.getInitiator());

        TransactionDTO dto2 = dtos.get(1);
        assertEquals(fullTransaction2.getAmount(), dto2.getAmount());
        assertEquals(fullTransaction2.getDescription(), dto2.getDescription());
        assertEquals(fullTransaction2.getCreatedAt(), dto2.getCreatedAt());
        assertEquals(fullTransaction2.getFromAccount().getIBAN().toString(), dto2.getSender());
        assertEquals(fullTransaction2.getToAccount().getIBAN().toString(), dto2.getReceiver());
        assertEquals(fullTransaction2.getInitiator().getId(), dto2.getInitiator());
    }

    @Test
    void testToDTOListWithEmptyList() {
        TransactionJpa transactionJpa = transactionService;
        List<TransactionDTO> dtos = transactionJpa.toDTO(new ArrayList<>());
        assertNotNull(dtos);
        assertEquals(0, dtos.size());
    }



}