package com.krachbank.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.iban4j.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.TransactionRepository;

public class TransactionJpaTest {

    TransactionJpa transactionService;
    AccountServiceJpa accountService;
    TransactionRepository transactionRepository;
    TransactionFilter transactionFilter;

    Transaction fullTransaction;
    Transaction fullTransaction2;
    List<Transaction> transactions;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        accountService = mock(AccountServiceJpa.class);
        transactionFilter = mock(TransactionFilter.class);

        transactionService = new TransactionJpa(transactionRepository);

        Iban iban = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        User initiator = new User();
        initiator.setId(100L);

         User initiator2 = new User();
        initiator2.setId(200L);

        
        Account fromAccount = new Account();
        fromAccount.setId(10L);
        fromAccount.setIBAN(iban);
        fromAccount.setUser(initiator);

        Account toAccount = new Account();
        toAccount.setIBAN(iban2);
        toAccount.setId(20L);
        fromAccount.setUser(initiator2);

        fullTransaction = new Transaction();
        fullTransaction.setId(1L);
        fullTransaction.setAmount(new BigDecimal("100.00"));
        fullTransaction.setDescription("Test transaction");
        fullTransaction.setCreatedAt(LocalDateTime.now());
        fullTransaction.setToAccount(toAccount);
        fullTransaction.setFromAccount(fromAccount);
        fullTransaction.setInitiator(initiator);

       



        Account fromAccount2 = new Account();
        fromAccount2.setId(30L);
        fromAccount2.setIBAN(Iban.valueOf("DE44500105175407324931"));
        fromAccount2.setUser(initiator2);

        Account toAccount2 = new Account();
        toAccount2.setId(40L);
        toAccount2.setIBAN(Iban.valueOf("DE12500105170648489890"));
        toAccount2.setUser(initiator);


        fullTransaction2 = new Transaction();
        fullTransaction2.setInitiator(initiator2);
        fullTransaction2.setId(2L);
        fullTransaction2.setAmount(new BigDecimal("250.50"));
        fullTransaction2.setDescription("Second test transaction");
        fullTransaction2.setCreatedAt(LocalDateTime.now().minusDays(1));
        fullTransaction2.setFromAccount(fromAccount2);
        fullTransaction2.setToAccount(toAccount2);

        

        transactions = List.of(fullTransaction, fullTransaction2);

        when(accountService.getAccountByIBAN(fullTransaction.getFromAccount().getIBAN()))
                .thenReturn(Optional.of(fullTransaction.getFromAccount()));
        when(accountService.getAccountByIBAN(fullTransaction.getToAccount().getIBAN()))
                .thenReturn(Optional.of(fullTransaction.getToAccount()));
        when(accountService.getAccountByIBAN(fullTransaction2.getFromAccount().getIBAN()))
                .thenReturn(Optional.of(fullTransaction2.getFromAccount()));
        when(accountService.getAccountByIBAN(fullTransaction2.getToAccount().getIBAN()))
                .thenReturn(Optional.of(fullTransaction2.getToAccount()));

        when(transactionRepository.findAll((org.springframework.data.jpa.domain.Specification<Transaction>) any()))
                .thenReturn(transactions);

        when(transactionRepository.findOne((org.springframework.data.jpa.domain.Specification<Transaction>) any()))
                .thenReturn(Optional.of(fullTransaction));
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

    // TODO: move this to the test class for the transaction Mapper
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

    @Test
    void testGetTransactionByFilterWithValidFilterReturnsTransaction() {

        Optional<Transaction> result = transactionService.getTransactionByFilter(transactionFilter);

        assertNotNull(result);
        assertEquals(fullTransaction, result.get());
    }

    @Test
    void testGetTransactionByFilterWithValidFilterReturnsEmpty() {

        when(transactionRepository.findOne((org.springframework.data.jpa.domain.Specification<Transaction>) any()))
                .thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.getTransactionByFilter(transactionFilter);

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testGetTransactionByFilterWithNullFilterThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactionByFilter(null));
    }

    @Test
    void testGetTransactionsByFilterWithValidFilterReturnsTransactions() {

        List<Transaction> result = transactionService.getTransactionsByFilter(transactionFilter);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(fullTransaction, result.get(0));
        assertEquals(fullTransaction2, result.get(1));
    }

    @Test
    void testGetTransactionsByFilterWithValidFilterReturnsEmptyList() {
        when(transactionRepository.findAll((org.springframework.data.jpa.domain.Specification<Transaction>) any()))
                .thenReturn(new ArrayList<>());

        List<Transaction> result = transactionService.getTransactionsByFilter(transactionFilter);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetTransactionsByFilterWithNullFilterThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactionsByFilter(null));
    }

    @Test
    void testGetTransactionByIdWithValidIdReturnsTransaction() throws Exception {
        Long id = fullTransaction.getId();
        when(transactionRepository.findById(id)).thenReturn(Optional.of(fullTransaction));

        Optional<Transaction> result = transactionService.getTransactionById(id);

        assertNotNull(result);
        assertEquals(fullTransaction, result.get());
    }

    @Test
    void testGetTransactionByIdWithNonExistingIdReturnsEmpty() throws Exception {
        Long id = 999L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.getTransactionById(id);

        assertNotNull(result);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testGetTransactionByIdWithNullIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactionById(null));
    }

    @Test
    void testGetTransactionByIdWithNegativeIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactionById(-1L));
    }

    @Test
    void testGetUserTotalAmountSpendAtDateWithTransactionsOnSameDay() {

        User user = fullTransaction.getInitiator();
        LocalDateTime date = fullTransaction.getCreatedAt();

        transactions = List.of(fullTransaction, fullTransaction2);

        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId()))
                .thenReturn(transactions);

        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId()))
                .thenReturn(transactions);

        BigDecimal total = transactionService.getUserTotalAmountSpendAtDate(user, date);

        assertEquals(new BigDecimal("100.00"), total);
    }

    @Test
    void testGetUserTotalAmountSpendAtDateWithTransactionsOnDifferentDays() {
        Transaction transaction1 = fullTransaction;
        Transaction transaction2 = fullTransaction;
        transaction2.setCreatedAt(transaction1.getCreatedAt().minusDays(1));
        User user = fullTransaction.getInitiator();
        LocalDateTime date = fullTransaction.getCreatedAt();

        List<Transaction> userTransactions = List.of(transaction1, transaction2);

        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId()))
                .thenReturn(userTransactions);

        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId()))
                .thenReturn(userTransactions);

        BigDecimal total = transactionService.getUserTotalAmountSpendAtDate(user, date);

        assertEquals(transaction1.getAmount(), total);
    }

    @Test
    void testGetUserTotalAmountSpendAtDateWithNoTransactions() {
        User user = new User();
        user.setId(100L);
        LocalDateTime date = LocalDateTime.of(2024, 6, 10, 12, 0);

        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId()))
                .thenReturn(new ArrayList<>());

        BigDecimal total = transactionService.getUserTotalAmountSpendAtDate(user, date);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void testGetUserTotalAmountSpendAtDateWithNullUser() {
        LocalDateTime date = LocalDateTime.of(2024, 6, 10, 12, 0);
        assertThrows(NullPointerException.class, () -> transactionService.getUserTotalAmountSpendAtDate(null, date));
    }

    @Test
    void testGetUserTotalAmountSpendAtDateWithNullDate() {
        User user = new User();
        user.setId(100L);
        assertThrows(NullPointerException.class, () -> transactionService.getUserTotalAmountSpendAtDate(user, null));
    }

}