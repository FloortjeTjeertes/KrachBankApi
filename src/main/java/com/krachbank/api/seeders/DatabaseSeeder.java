package com.krachbank.api.seeders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.krachbank.api.configuration.IBANGenerator;
import com.krachbank.api.controllers.UserController;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.AccountRepository;
import com.krachbank.api.repository.TransactionRepository;
import com.krachbank.api.repository.UserRepository;
import com.krachbank.api.service.UserService;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseSeeder {

    private final UserController userController;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public DatabaseSeeder(UserRepository userRepository, AccountRepository accountRepository,
                          TransactionRepository transactionRepository,UserService userService, UserController userController) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.userController = userController;
    }

    @PostConstruct
    public void seed() {
        // Create users
        // TODO: passwords need to be hashed acording to the security and authentication
        // methods
        User user1 = new User();
        user1.setFirstName("Alice");
        user1.setLastName("Smith");
        user1.setUsername("Alice Smith");
        user1.setDailyLimit(new BigDecimal("1000.00"));
        user1.setEmail("alice@example.com");
        user1.setPassword("password123");
        user1.setPhoneNumber("+491234567890");
        user1.setCreatedAt(LocalDateTime.now());
        user1.setVerified(true); // Assuming users are not verified by default
        UserDTO savedUser1 = userService.createUser(userService.toDTO(user1));

        User user2 = new User();
        user2.setFirstName("Bob");
        user2.setLastName("Johnson");
        user2.setUsername("Bob Johnson");
        user2.setDailyLimit(new BigDecimal("500.00"));
        user2.setEmail("bob@example.com");
        user2.setPassword("password123");
        user2.setPhoneNumber("+491234567891");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setVerified(true); // Assuming users are not verified by default
        UserDTO savedUser2 = userService.createUser(userService.toDTO(user2));
        

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setUsername("Admin User");
        admin.setDailyLimit(new BigDecimal("200.00"));
        admin.setEmail("admin@example.com");
        admin.setPassword("adminpass");
        admin.setPhoneNumber("+491234567892");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setVerified(true); // Assuming admins are verified by default
        UserDTO savedAdmin = userService.createUser(userService.toDTO(admin));
        // Create accounts
        Account account1 = new Account();
        account1.setIban(IBANGenerator.generateIBAN());
        account1.setAbsoluteLimit(new BigDecimal(-100));
        account1.setAccountType(AccountType.CHECKING);
        account1.setBalance(new BigDecimal("5000.00"));
        account1.setTransactionLimit(new BigDecimal(1000));
        account1.setCreatedAt(LocalDateTime.now());
        account1.setUser(userController.toModel(savedUser1));
        account1.setVerifiedBy(userController.toModel(savedAdmin));
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setIban(IBANGenerator.generateIBAN());
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setUser(userController.toModel(savedUser1));
        account2.setAccountType(AccountType.SAVINGS);
        account2.setAbsoluteLimit(new BigDecimal(-100));
        account2.setTransactionLimit(new BigDecimal(1000));
        account2.setCreatedAt(LocalDateTime.now());
        account2.setVerifiedBy(userController.toModel(savedAdmin));
        accountRepository.save(account2);

        Account account3 = new Account();
        account3.setIban(IBANGenerator.generateIBAN());
        account3.setBalance(new BigDecimal("2000.00"));
        account3.setUser(userController.toModel(savedUser2));
        account3.setAccountType(AccountType.SAVINGS);
        account3.setAbsoluteLimit(new BigDecimal(-100));
        account3.setTransactionLimit(new BigDecimal(1000));
        account3.setCreatedAt(LocalDateTime.now());
        account3.setVerifiedBy(userController.toModel(savedAdmin));
        accountRepository.save(account3);


        Account account4 = new Account();
        account4.setIban(IBANGenerator.generateIBAN());
        account4.setBalance(new BigDecimal("2000.00"));
        account4.setUser(userController.toModel(savedUser2));
        account4.setAccountType(AccountType.CHECKING);
        account4.setAbsoluteLimit(new BigDecimal(-100));
        account4.setTransactionLimit(new BigDecimal(1000));
        account4.setCreatedAt(LocalDateTime.now());
        account4.setVerifiedBy(userController.toModel(savedAdmin));
        accountRepository.save(account4);



        // make an test Transaction
        Transaction transaction = new com.krachbank.api.models.Transaction();
        transaction.setAmount(new BigDecimal("250.00"));
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setFromAccount(account1);
        transaction.setInitiator(userController.toModel(savedUser1));
        transaction.setToAccount(account2);
        transaction.setDescription("Test transaction from Alice to Bob");

        // Assuming you have a TransactionRepository and it's injected similarly
        transactionRepository.save(transaction);

    }
}