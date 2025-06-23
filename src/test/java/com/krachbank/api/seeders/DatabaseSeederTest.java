package com.krachbank.api.seeders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.mappers.UserMapper;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.AccountRepository;
import com.krachbank.api.repository.TransactionRepository;
import com.krachbank.api.service.UserService;

class DatabaseSeederTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private UserService userService;
    private UserMapper userMapper;
    private DatabaseSeeder databaseSeeder;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        userService = mock(UserService.class);
        userMapper = mock(UserMapper.class);

        // Mock UserService.createUser and toDTO
        when(userService.createUser(any(UserDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.toDTO(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            UserDTO dto = new UserDTO();
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            return dto;
        });

        // Mock UserMapper.toModel
        when(userMapper.toModel(any(UserDTO.class))).thenAnswer(invocation -> {
            UserDTO dto = invocation.getArgument(0);
            User user = new User();
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            return user;
        });

        databaseSeeder = new DatabaseSeeder(accountRepository, transactionRepository, userService, userMapper);
    }

    @Test
    void testSeedCreatesUsersAccountsAndTransaction() {
        databaseSeeder.seed();

        // Verify users are created
        verify(userService, times(4)).createUser(any(UserDTO.class));

        // Verify accounts are saved
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(3)).save(accountCaptor.capture());

        // Check account properties
        boolean foundSavings = false, foundChecking = false, foundATM = false;
        for (Account acc : accountCaptor.getAllValues()) {
            if (acc.getAccountType() == AccountType.SAVINGS)
                foundSavings = true;
            if (acc.getAccountType() == AccountType.CHECKING
                    && acc.getBalance().compareTo(new BigDecimal("2000.00")) == 0)
                foundChecking = true;
            if (acc.getAccountType() == AccountType.CHECKING
                    && acc.getBalance().compareTo(new BigDecimal("10000.00")) == 0)
                foundATM = true;
        }
        assert (foundSavings);
        assert (foundChecking);
        assert (foundATM);

        // Verify transaction is saved
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        Transaction transaction = transactionCaptor.getValue();
        assert (transaction.getAmount().compareTo(new BigDecimal("250.00")) == 0);
        assert ("Test transaction from Alice to Bob".equals(transaction.getDescription()));
        assert (transaction.getCreatedAt() != null);
        assert (transaction.getFromAccount() != null);
        assert (transaction.getToAccount() != null);
        assert (transaction.getInitiator() != null);
    }
}