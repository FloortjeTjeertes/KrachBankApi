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
import org.springframework.data.domain.Page;


import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.TransactionRepository;
import com.krachbank.api.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
public class TransactionServiceJpaTest {

    TransactionServiceJpa transactionService;
    AccountServiceJpa accountService;
    TransactionRepository transactionRepository;
    UserRepository userRepository;
    TransactionFilter transactionFilter;

    Transaction fullTransaction;
    Transaction fullTransaction2;
    Transaction fullTransaction3;
    List<Transaction> transactions;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        userRepository = mock(UserRepository.class);
        accountService = mock(AccountServiceJpa.class);
        transactionFilter = mock(TransactionFilter.class);

        transactionService = new TransactionServiceJpa(transactionRepository, accountService, userRepository);

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
        account2.setBalance(new BigDecimal("1000.00"));

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
        fullTransaction3.setId(3L);
        fullTransaction3.setAmount(new BigDecimal("350"));
        fullTransaction3.setDescription("Second test transaction");
        fullTransaction3.setCreatedAt(LocalDateTime.now());
        fullTransaction3.setFromAccount(account1);
        fullTransaction3.setToAccount(account2);

        transactions = List.of(fullTransaction, fullTransaction2);

        when(accountService.getAccountByIBAN(account1.getIban().toString()))
                .thenReturn(Optional.of(account1));
        when(accountService.getAccountByIBAN(account2.getIban().toString()))
                .thenReturn(Optional.of(account2));

        when(transactionRepository.findAll((Specification<Transaction>) any()))
                .thenReturn(transactions);

        when(transactionRepository.findOne((Specification<Transaction>) any()))
                .thenReturn(Optional.of(fullTransaction));
    }

    @Test
    void testIsValidTransactionWithValidTransaction() {
        boolean result = transactionService.isValidTransaction(fullTransaction);

        assertEquals(true, result);
    }

    @Test
    void testIsValidTransactionWithZeroAmount() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.ZERO);
        transaction.setFromAccount(fullTransaction.getFromAccount());
        transaction.setToAccount(fullTransaction.getToAccount());
        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithNegativeAmount() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(-1000));
        transaction.setFromAccount(fullTransaction.getFromAccount());
        transaction.setToAccount(fullTransaction.getToAccount());
        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithNullFromAccount() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setFromAccount(null);
        transaction.setToAccount(fullTransaction.getToAccount());
        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithNullToAccount() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setFromAccount(fullTransaction.getFromAccount());
        transaction.setToAccount(null);
        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    @Test
    void testIsValidTransactionWithSameAccounts() {
        Transaction transaction = new Transaction();
        Account account = new Account();
        account.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setFromAccount(account);
        transaction.setToAccount(account);
        assertThrows(IllegalArgumentException.class, () -> transactionService.isValidTransaction(transaction));
    }

    // TODO: move this to the test class for the transaction Mapper
    @Test
    void testToDTOWithFullTransaction() {

        TransactionDTOResponse dto = transactionService.toDTO(fullTransaction);

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

        List<TransactionDTOResponse> dtos = transactionService.toDTO(transactions);

        assertNotNull(dtos);
        assertEquals(2, dtos.size());

        TransactionDTOResponse dto1 = dtos.get(0);
        assertEquals(fullTransaction.getAmount(), dto1.getAmount());
        assertEquals(fullTransaction.getDescription(), dto1.getDescription());
        assertEquals(fullTransaction.getCreatedAt(), dto1.getCreatedAt());
        assertEquals(fullTransaction.getFromAccount().getIban().toString(), dto1.getSender());
        assertEquals(fullTransaction.getToAccount().getIban().toString(), dto1.getReceiver());
        assertEquals(fullTransaction.getInitiator().getId(), dto1.getInitiator());

        TransactionDTOResponse dto2 = dtos.get(1);
        assertEquals(fullTransaction2.getAmount(), dto2.getAmount());
        assertEquals(fullTransaction2.getDescription(), dto2.getDescription());
        assertEquals(fullTransaction2.getCreatedAt(), dto2.getCreatedAt());
        assertEquals(fullTransaction2.getFromAccount().getIban().toString(), dto2.getSender());
        assertEquals(fullTransaction2.getToAccount().getIban().toString(), dto2.getReceiver());
        assertEquals(fullTransaction2.getInitiator().getId(), dto2.getInitiator());
    }

    @Test
    void testToDTOListWithEmptyList() {
        TransactionServiceJpa TransactionServiceJpa = transactionService;
        List<TransactionDTOResponse> dtos = TransactionServiceJpa.toDTO(new ArrayList<>());
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

        when(transactionRepository.findOne((Specification<Transaction>) any()))
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

        Page<Transaction> result = transactionService.getTransactionsByFilter(transactionFilter);

        assertNotNull(result);
        assertEquals(2, result.getSize());
        assertEquals(fullTransaction, result.getContent().get(0));
        assertEquals(fullTransaction2, result.getContent().get(1));
    }

    @Test
    void testGetTransactionsByFilterWithValidFilterReturnsEmptyList() {
        when(transactionRepository.findAll((Specification<Transaction>) any()))
                .thenReturn(new ArrayList<>());

        Page<Transaction> result = transactionService.getTransactionsByFilter(transactionFilter);

        assertNotNull(result);
        assertEquals(0, result.getSize());
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
        User user1 = new User();
        user1.setId(100L);
        user1.setDailyLimit(new BigDecimal("10000.00"));

        User user2 = new User();
        user2.setId(200L);

        Iban iban1 = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        Account sendingAccount = new Account();
        sendingAccount.setId(10L);
        sendingAccount.setIban(iban1);
        sendingAccount.setUser(user1);
        sendingAccount.setBalance(new BigDecimal("1000.00"));
        sendingAccount.setAccountType(AccountType.CHECKING);
        sendingAccount.setAbsoluteLimit(new BigDecimal("-500.00"));
        sendingAccount.setTransactionLimit(new BigDecimal("1000.00"));

        Account receivingAccount = new Account();
        receivingAccount.setId(20L);
        receivingAccount.setIban(iban2);
        receivingAccount.setUser(user2);
        receivingAccount.setBalance(new BigDecimal("1000.00"));
        receivingAccount.setAccountType(AccountType.CHECKING);

        Transaction transaction = new Transaction();
        transaction.setId(100L);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setFromAccount(sendingAccount);
        transaction.setToAccount(receivingAccount);
        transaction.setInitiator(user1);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(accountService.getAccountByIBAN(iban1.toString())).thenReturn(Optional.of(sendingAccount));
        when(accountService.getAccountByIBAN(iban2.toString())).thenReturn(Optional.of(receivingAccount));
        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user1.getId())).thenReturn(List.of());

        Optional<Transaction> result = transactionService.createTransaction(transaction, user1.getUsername());

        assertNotNull(result);
        assertEquals(transaction, result.get());
    }

    @Test
    void testCreateTransactionThrowsIfTransactionAlreadyExists() {
        User user = new User();
        user.setId(100L);

        Transaction transaction = new Transaction();
        transaction.setId(101L);
        transaction.setInitiator(user);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        Exception exeption = assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(transaction, user.getUsername()));
        assertEquals("Transaction already exists", exeption.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfNotInternalTransaction() {
        User user = new User();
        user.setId(100L);

        Iban iban1 = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("AL62134113212579341242716778");

        Account fromAccount = new Account();
        fromAccount.setIban(iban1);
        fromAccount.setUser(user);

        Account toAccount = new Account();
        toAccount.setIban(iban2);
        toAccount.setUser(new User());

        Transaction transaction = new Transaction();
        transaction.setId(103L);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setInitiator(user);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(accountService.getAccountByIBAN(iban1.toString())).thenReturn(Optional.of(fromAccount));
        when(accountService.getAccountByIBAN(iban2.toString())).thenReturn(Optional.of(toAccount));

        Exception exception = assertThrows(Exception.class,
                () -> transactionService.createTransaction(transaction, user.getUsername()));
        assertEquals("this transaction is not whit accounts from our bank", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfSameAccount() {
        User user = new User();
        user.setId(100L);

        Iban iban = Iban.valueOf("DE22500211208825963824");
        Account account = new Account();
        account.setIban(iban);
        account.setId(1L);
        account.setUser(user);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
        account.setAbsoluteLimit(new BigDecimal("-500.00"));
        account.setTransactionLimit(new BigDecimal("1000.00"));

        Transaction transaction = new Transaction();
        transaction.setId(104L);
        transaction.setFromAccount(account);
        transaction.setToAccount(account);
        transaction.setInitiator(user);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(accountService.getAccountByIBAN(iban.toString())).thenReturn(Optional.of(account));

        Exception exception = assertThrows(Exception.class,
                () -> transactionService.createTransaction(transaction, user.getUsername()));
        assertEquals("cant transfer to the same account", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfSavingsAccountAndDifferentUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setDailyLimit(new BigDecimal("10000.00"));
        User user2 = new User();
        user2.setId(2L);

        Iban iban1 = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        Account from = new Account();
        from.setIban(iban1);
        from.setAccountType(AccountType.SAVINGS);
        from.setUser(user1);
        from.setBalance(new BigDecimal("1000.00"));
        from.setAbsoluteLimit(new BigDecimal("-500.00"));
        from.setTransactionLimit(new BigDecimal("1000.00"));

        Account to = new Account();
        to.setIban(iban2);
        to.setAccountType(AccountType.CHECKING);
        to.setUser(user2);

        Transaction transaction = new Transaction();
        transaction.setId(105L);
        transaction.setFromAccount(from);
        transaction.setToAccount(to);
        transaction.setInitiator(user1);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(accountService.getAccountByIBAN(iban1.toString())).thenReturn(Optional.of(from));
        when(accountService.getAccountByIBAN(iban2.toString())).thenReturn(Optional.of(to));

        Exception exception = assertThrows(Exception.class,
                () -> transactionService.createTransaction(transaction, user1.getUsername()));
        assertEquals("cant transfer money to or from another persons saving account", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfReachedAbsoluteLimit() {
        User user = new User();
        user.setId(100L);
        user.setDailyLimit(new BigDecimal("10000.00"));

        Iban iban1 = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        Account from = new Account();
        from.setIban(iban1);
        from.setAccountType(AccountType.CHECKING);
        from.setUser(user);
        from.setBalance(new BigDecimal("10.00"));
        from.setAbsoluteLimit(new BigDecimal("0.00"));
        from.setTransactionLimit(new BigDecimal("1000.00"));

        Account to = new Account();
        to.setIban(iban2);
        to.setAccountType(AccountType.CHECKING);

        Transaction transaction = new Transaction();
        transaction.setId(106L);
        transaction.setFromAccount(from);
        transaction.setToAccount(to);
        transaction.setInitiator(user);
        transaction.setAmount(new BigDecimal("20.00"));

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(accountService.getAccountByIBAN(iban1.toString())).thenReturn(Optional.of(from));
        when(accountService.getAccountByIBAN(iban2.toString())).thenReturn(Optional.of(to));
        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId())).thenReturn(List.of());

        Exception exception = assertThrows(Exception.class,
                () -> transactionService.createTransaction(transaction, user.getUsername()));
        assertEquals("cant spend more then the absolute limit", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfReachedDailyTransferLimit() {
        User user = new User();
        user.setId(100L);
        user.setDailyLimit(new BigDecimal("50.00"));

        Iban iban1 = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        Account from = new Account();
        from.setIban(iban1);
        from.setAccountType(AccountType.CHECKING);
        from.setUser(user);
        from.setBalance(new BigDecimal("1000.00"));
        from.setAbsoluteLimit(new BigDecimal("-500.00"));
        from.setTransactionLimit(new BigDecimal("1000.00"));

        Account to = new Account();
        to.setIban(iban2);
        to.setAccountType(AccountType.CHECKING);

        Transaction transaction = new Transaction();
        transaction.setId(107L);
        transaction.setFromAccount(from);
        transaction.setToAccount(to);
        transaction.setInitiator(user);
        transaction.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(accountService.getAccountByIBAN(iban1.toString())).thenReturn(Optional.of(from));
        when(accountService.getAccountByIBAN(iban2.toString())).thenReturn(Optional.of(to));
        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId()))
                .thenReturn(List.of(transaction));

        Exception exception = assertThrows(Exception.class,
                () -> transactionService.createTransaction(transaction, user.getUsername()));
        assertEquals("daily limit reached", exception.getMessage());
    }

    @Test
    void testCreateTransactionThrowsIfTransferAmountBiggerThanLimit() {
        User user = new User();
        user.setId(100L);
        user.setDailyLimit(new BigDecimal("10000.00"));

        Iban iban1 = Iban.valueOf("DE32500211205487556354");
        Iban iban2 = Iban.valueOf("DE52500202006796187625");

        Account from = new Account();
        from.setIban(iban1);
        from.setAccountType(AccountType.CHECKING);
        from.setUser(user);
        from.setBalance(new BigDecimal("1000.00"));
        from.setAbsoluteLimit(new BigDecimal("-500.00"));
        from.setTransactionLimit(new BigDecimal("50.00"));

        Account to = new Account();
        to.setIban(iban2);
        to.setAccountType(AccountType.CHECKING);

        Transaction transaction = new Transaction();
        transaction.setId(108L);
        transaction.setFromAccount(from);
        transaction.setToAccount(to);
        transaction.setInitiator(user);
        transaction.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());
        when(accountService.getAccountByIBAN(iban1.toString())).thenReturn(Optional.of(from));
        when(accountService.getAccountByIBAN(iban2.toString())).thenReturn(Optional.of(to));
        when(transactionRepository.findByInitiatorIdOrderByCreatedAtAsc(user.getId())).thenReturn(List.of());

        Exception exception = assertThrows(Exception.class,
                () -> transactionService.createTransaction(transaction, user.getUsername()));
        assertEquals("this amount is more than your transfer limit of the account", exception.getMessage());
    }

 
  

}