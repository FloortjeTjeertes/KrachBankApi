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
import com.krachbank.api.models.AccountType;

public class TransactionJpaTest {

    TransactionJpa transactionService;
    AccountServiceJpa accountService;
    TransactionRepository transactionRepository;
    TransactionFilter transactionFilter;

    Transaction fullTransaction;
    Transaction fullTransaction2;
    Transaction fullTransaction3;
    List<Transaction> transactions;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        accountService = mock(AccountServiceJpa.class);
        transactionFilter = mock(TransactionFilter.class);

        transactionService = new TransactionJpa(transactionRepository, accountService);

        Iban iban = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        User user1 = new User();
        user1.setId(100L);

        User user2 = new User();
        user2.setId(200L);

        Account account1 = new Account();
        account1.setId(10L);
        account1.setIban(iban);
        account1.setUser(user1);
        account1.setBalance(new BigDecimal("1000.00"));

        Account account2 = new Account();
        account2.setIban(iban2);
        account2.setId(20L);
        account2.setUser(user2);
        account1.setBalance(new BigDecimal("1000.00"));

        fullTransaction = new Transaction();
        fullTransaction.setId(1L);
        fullTransaction.setAmount(new BigDecimal("100.00"));
        fullTransaction.setDescription("Test transaction");
        fullTransaction.setCreatedAt(LocalDateTime.now());
        fullTransaction.setFromAccount(account1);
        fullTransaction.setToAccount(account2);
        fullTransaction.setInitiator(user1);

        fullTransaction2 = new Transaction();
        fullTransaction2.setInitiator(user2);
        fullTransaction2.setId(2L);
        fullTransaction2.setAmount(new BigDecimal("250.50"));
        fullTransaction2.setDescription("Second test transaction");
        fullTransaction2.setCreatedAt(LocalDateTime.now().minusDays(1));
        fullTransaction2.setFromAccount(account2);
        fullTransaction2.setToAccount(account1);

        fullTransaction3 = new Transaction();
        fullTransaction3.setInitiator(user1);
        fullTransaction3.setId(2L);
        fullTransaction3.setAmount(new BigDecimal("350"));
        fullTransaction3.setDescription("Second test transaction");
        fullTransaction3.setCreatedAt(LocalDateTime.now());
        fullTransaction3.setFromAccount(account1);
        fullTransaction3.setToAccount(account2);

        transactions = List.of(fullTransaction, fullTransaction2);

        when(accountService.getAccountByIBAN(fullTransaction.getFromAccount().getIban().toString()))
                .thenReturn(Optional.of(fullTransaction.getFromAccount()));
        when(accountService.getAccountByIBAN(fullTransaction.getFromAccount().getIban().toString()))
                .thenReturn(Optional.of(fullTransaction.getToAccount()));
        when(accountService.getAccountByIBAN(fullTransaction.getFromAccount().getIban().toString()))
                .thenReturn(Optional.of(fullTransaction2.getFromAccount()));
        when(accountService.getAccountByIBAN(fullTransaction.getFromAccount().getIban().toString()))
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
        assertEquals(fullTransaction.getFromAccount().getIban().toString(), dto.getSender());
        assertEquals(fullTransaction.getToAccount().getIban().toString(), dto.getReceiver());
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
        assertEquals(fullTransaction.getFromAccount().getIban().toString(), dto1.getSender());
        assertEquals(fullTransaction.getToAccount().getIban().toString(), dto1.getReceiver());
        assertEquals(fullTransaction.getInitiator().getId(), dto1.getInitiator());

        TransactionDTO dto2 = dtos.get(1);
        assertEquals(fullTransaction2.getAmount(), dto2.getAmount());
        assertEquals(fullTransaction2.getDescription(), dto2.getDescription());
        assertEquals(fullTransaction2.getCreatedAt(), dto2.getCreatedAt());
        assertEquals(fullTransaction2.getFromAccount().getIban().toString(), dto2.getSender());
        assertEquals(fullTransaction2.getToAccount().getIban().toString(), dto2.getReceiver());
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
        fullTransaction.setCreatedAt(date);
        fullTransaction.setAmount(BigDecimal.valueOf(100.00));
        fullTransaction3.setCreatedAt(date);
        fullTransaction3.setAmount(BigDecimal.valueOf(150.00));

        List<Transaction> testTransactions = List.of(fullTransaction, fullTransaction3);

        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId()))
                .thenReturn(testTransactions);

        BigDecimal total = transactionService.getUserTotalAmountSpendAtDate(user, date);

        assertEquals(new BigDecimal("250.0"), total);
    }

    @Test
    void testGetUserTotalAmountSpendAtDateWithTransactionsOnDifferentDays() {
        Transaction transaction1 = fullTransaction;
        Transaction transaction2 = fullTransaction2;
        transaction2.setCreatedAt(transaction1.getCreatedAt().minusDays(1));
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction2.setAmount(new BigDecimal("200.00"));

        User user = fullTransaction.getInitiator();
        LocalDateTime date = fullTransaction.getCreatedAt();

        List<Transaction> userTransactions = List.of(transaction1, transaction2);

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
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.getUserTotalAmountSpendAtDate(null, date));
    }

    @Test
    void testGetUserTotalAmountSpendAtDateWithNullDate() {
        User user = new User();
        user.setId(100L);
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.getUserTotalAmountSpendAtDate(user, null));
    }

    @Test
    void testIsInternalTransactionWithSameBankCode() {
        // Use presetup accounts from setUp()
        Account account1 = fullTransaction.getFromAccount();
        Account account2 = fullTransaction.getToAccount();

        // Set both accounts to have the same bank code
        Iban iban = fullTransaction.getFromAccount().getIban();
        account1.setIban(iban);
        account2.setIban(iban);

        boolean result = transactionService.IsInternalTransaction(account1, account2);

        assertEquals(iban.getBankCode().equals(account2.getIban().getBankCode()), result);
    }

    @Test
    void testIsInternalTransactionWithDifferentBankCode() {
        // Use presetup accounts from setUp()
        Account account1 = fullTransaction.getFromAccount();
        Account account2 = fullTransaction.getToAccount();

        // Set accounts to have different bank codes
        account1.setIban(fullTransaction.getFromAccount().getIban());
        account2.setIban(Iban.valueOf("DE64300205007996745665"));

        boolean result = transactionService.IsInternalTransaction(account1, account2);

        assertEquals(account1.getIban().getBankCode().equals(account2.getIban().getBankCode()), result);
    }

    @Test
    void testIsInternalTransactionWithNullAccountsThrowsException() {
        Account account1 = fullTransaction.getFromAccount();

        assertThrows(NullPointerException.class, () -> transactionService.IsInternalTransaction(null, account1));
        assertThrows(NullPointerException.class, () -> transactionService.IsInternalTransaction(account1, null));
    }

    @Test
    void testIsInternalTransactionWithNullIbansThrowsException() {
        Account account1 = fullTransaction.getFromAccount();
        Account account2 = fullTransaction.getToAccount();

        account1.setIban(null);
        account2.setIban(null);

        assertThrows(NullPointerException.class, () -> transactionService.IsInternalTransaction(account1, account2));
    }

    @Test
    void testCreateTransactionSuccess() throws Exception {
        Transaction transaction = fullTransaction;
        transaction.setId(100L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Set up accounts
        Account sendingAccount = transaction.getFromAccount();
        Account receivingAccount = transaction.getToAccount();

        // Set up account types and limits
        sendingAccount.setAccountType(AccountType.CHECKING);
        sendingAccount.setBalance(new BigDecimal("1000.00"));
        sendingAccount.setAbsoluteLimit(new BigDecimal("-500.00"));
        sendingAccount.setTransactionLimit(new BigDecimal("1000.00"));
        sendingAccount.getUser().setDailyLimit(new BigDecimal("10000.00"));
        sendingAccount.setUser(transaction.getInitiator());

        receivingAccount.setUser(fullTransaction2.getInitiator());
        receivingAccount.setBalance(new BigDecimal("1000.00"));
        receivingAccount.setAccountType(AccountType.CHECKING);

        Optional<Transaction> result = transactionService.createTransaction(transaction);

        assertNotNull(result);
        assertEquals(transaction, result.get());
    }

    @Test
    void testCreateTransactionThrowsIfTransactionAlreadyExists() {
        Transaction transaction = fullTransaction;
        transaction.setId(101L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        Exception exeption = assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(transaction));
        assertEquals("Transaction already exists", exeption.getMessage());
    }

 

    @Test
    void testCreateTransactionThrowsIfNotInternalTransaction() {
        Transaction transaction = fullTransaction;
        transaction.setId(103L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());

        // Set both accounts to have the diverent bank code
        Iban iban = Iban.valueOf("DE32500211205487556354");
        Iban invalidIban = Iban.valueOf("AL62134113212579341242716778");
        transaction.getFromAccount().setIban(iban);
        transaction.getToAccount().setIban(invalidIban);

        Exception exception = assertThrows(Exception.class, () -> transactionService.createTransaction(transaction));
        assertEquals("this transaction is not whit accounts from our bank",exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfSameAccount() {
        Transaction transaction = fullTransaction;
        transaction.setId(104L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());

        Account account = new Account();
        Iban iban = Iban.valueOf("DE22500211208825963824");
        account.setIban(iban);
        account.setId(1L);
        account.setUser(new User());
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
        account.setAbsoluteLimit(new BigDecimal("-500.00"));
        account.setTransactionLimit(new BigDecimal("1000.00"));

        Account account2 = new Account();
        account2.setIban(iban);

        transaction.setFromAccount(account);

        transaction.setToAccount(account2);

       Exception exception = assertThrows(Exception.class, () -> transactionService.createTransaction(transaction));
        assertEquals("cant transfer to the same account", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfSavingsAccountAndDifferentUsers() {
        Transaction transaction = fullTransaction;
        transaction.setId(105L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());

        Account from = transaction.getFromAccount();
        Account to = transaction.getToAccount();

        from.setAccountType(AccountType.SAVINGS);
        to.setAccountType(AccountType.CHECKING);

        // Set different users
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        from.setUser(user1);
        to.setUser(user2);

        from.setBalance(new BigDecimal("1000.00"));
        from.setAbsoluteLimit(new BigDecimal("-500.00"));
        from.setTransactionLimit(new BigDecimal("1000.00"));
        user1.setDailyLimit(new BigDecimal("10000.00"));

        Exception exception = assertThrows(Exception.class, () -> transactionService.createTransaction(transaction));
        assertEquals("cant transfer money to or from another persons saving account", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfReachedAbsoluteLimit() {
        Transaction transaction = fullTransaction;
        transaction.setId(106L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());

        Account from = transaction.getFromAccount();
        Account to = transaction.getToAccount();

        from.setAccountType(AccountType.CHECKING);
        to.setAccountType(AccountType.CHECKING);

        from.setBalance(new BigDecimal("10.00"));
        from.setAbsoluteLimit(new BigDecimal("0.00"));
        from.setTransactionLimit(new BigDecimal("1000.00"));
        from.setUser(transaction.getInitiator());
        from.getUser().setDailyLimit(new BigDecimal("10000.00"));

        transaction.setAmount(new BigDecimal("20.00"));

        Exception exception =assertThrows(Exception.class, () -> transactionService.createTransaction(transaction));
        assertEquals("cant spend more then the absolute limit", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfReachedDailyTransferLimit() {
        Transaction transaction = fullTransaction;
        transaction.setId(107L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());

        Account from = transaction.getFromAccount();
        Account to = transaction.getToAccount();

        from.setAccountType(AccountType.CHECKING);
        to.setAccountType(AccountType.CHECKING);

        from.setBalance(new BigDecimal("1000.00"));
        from.setAbsoluteLimit(new BigDecimal("-500.00"));
        from.setTransactionLimit(new BigDecimal("1000.00"));
        from.setUser(transaction.getInitiator());
        from.getUser().setDailyLimit(new BigDecimal("50.00"));

        transaction.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(from.getUser().getId()))
                .thenReturn(List.of(transaction));

       Exception exception = assertThrows(Exception.class, () -> transactionService.createTransaction(transaction));
        assertEquals("daily limit reached", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfTransferAmountBiggerThanLimit() {
        Transaction transaction = fullTransaction;
        transaction.setId(108L);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());

        Account from = transaction.getFromAccount();
        Account to = transaction.getToAccount();

        from.setAccountType(AccountType.CHECKING);
        to.setAccountType(AccountType.CHECKING);

        from.setBalance(new BigDecimal("1000.00"));
        from.setAbsoluteLimit(new BigDecimal("-500.00"));
        from.setTransactionLimit(new BigDecimal("50.00"));
        from.setUser(transaction.getInitiator());
        from.getUser().setDailyLimit(new BigDecimal("10000.00"));

        transaction.setAmount(new BigDecimal("100.00"));

       Exception exception = assertThrows(Exception.class, () -> transactionService.createTransaction(transaction));
        assertEquals("this amount is more than your transfer limit of the account", exception.getMessage());
    }

    @Test
    void testGetAllTransactionsReturnsAllTransactions() {
        List<Transaction> expectedTransactions = List.of(fullTransaction, fullTransaction2);
        when(transactionRepository.findAll()).thenReturn(expectedTransactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertEquals(expectedTransactions.size(), result.size());
        assertEquals(expectedTransactions, result);
    }

    @Test
    void testGetAllTransactionsReturnsEmptyList() {
        when(transactionRepository.findAll()).thenReturn(new ArrayList<>());

        List<Transaction> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

}