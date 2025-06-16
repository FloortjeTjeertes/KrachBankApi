package com.krachbank.api.seeders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.krachbank.api.configuration.IBANGenerator;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.mappers.UserMapper;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.AccountRepository;
import com.krachbank.api.repository.TransactionRepository;
import com.krachbank.api.service.UserService;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseSeeder {


        private final AccountRepository accountRepository;
        private final TransactionRepository transactionRepository;
        private final UserService userService;
        private final UserMapper userMapper;

        public DatabaseSeeder(AccountRepository accountRepository,
                        TransactionRepository transactionRepository, UserService userService,
                         UserMapper userMapper) {
                this.accountRepository = accountRepository;
                this.transactionRepository = transactionRepository;
                this.userService = userService;
                this.userMapper = userMapper;
        }

        @PostConstruct
        public void seed() {
                User user1 = new User();
                user1.setFirstName("Alice");
                user1.setLastName("Smith");
                user1.setUsername("AliceSmith");
                user1.setEmail("Alice@example.com");
                user1.setBSN(123456789);
                user1.setDailyLimit(new BigDecimal("1000.00"));
                user1.setPassword("password123");
                user1.setPhoneNumber("+491234567890");
                user1.setCreatedAt(LocalDateTime.now());
                user1.setVerified(false); // Assuming users are not verified by default
                user1.setTransferLimit(new BigDecimal("500.00"));
                UserDTO savedUser1 = userService.createUser(userService.toDTO(user1));

                User user2 = new User();
                user2.setFirstName("Bob");
                user2.setLastName("Johnson");
                user2.setUsername("BobJohnson");
                user2.setBSN(987654321);
                user2.setDailyLimit(new BigDecimal("500.00"));
                user2.setEmail("bob@example.com");
                user2.setPassword("password123");
                user2.setPhoneNumber("+491234567891");
                user2.setCreatedAt(LocalDateTime.now());
                user2.setVerified(true); // Assuming users are not verified by default
                user2.setTransferLimit(new BigDecimal("300.00"));
                UserDTO savedUser2 = userService.createUser(userService.toDTO(user2));

                User ATM = new User();
                ATM.setFirstName("ATM");
                ATM.setLastName("User");
                ATM.setUsername("ATMUser");
                ATM.setBSN(111223344);
                ATM.setDailyLimit(new BigDecimal("2000.00"));
                ATM.setEmail("ATM@ATM.com");
                ATM.setPassword("password");
                ATM.setPhoneNumber("+66666666666");
                ATM.setCreatedAt(LocalDateTime.now());
                ATM.setVerified(true);
                UserDTO savedATM = userService.createUser(userService.toDTO(ATM));

                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setUsername("AdminUser");
                admin.setBSN(112233445);
                admin.setDailyLimit(new BigDecimal("200.00"));
                admin.setEmail("admin@example.com");
                admin.setPassword("adminpass");
                admin.setPhoneNumber("+491234567892");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setVerified(true); // Assuming admins are verified by default
                admin.setAdmin(true);
                UserDTO savedAdmin = userService.createUser(userService.toDTO(admin));
                // Create accounts
  
                Account account3 = new Account();
                account3.setIban(IBANGenerator.generateIBAN());
                account3.setBalance(new BigDecimal("2000.00"));
                account3.setUser(userMapper.toModel(savedUser2));
                account3.setAccountType(AccountType.SAVINGS);
                account3.setAbsoluteLimit(new BigDecimal(-100));
                account3.setTransactionLimit(new BigDecimal(1000));
                account3.setCreatedAt(LocalDateTime.now());
                account3.setVerifiedBy(userMapper.toModel(savedAdmin));
                accountRepository.save(account3);

                Account account4 = new Account();
                account4.setIban(IBANGenerator.generateIBAN());
                account4.setBalance(new BigDecimal("2000.00"));
                account4.setUser(userMapper.toModel(savedUser2));
                account4.setAccountType(AccountType.CHECKING);
                account4.setAbsoluteLimit(new BigDecimal(-100));
                account4.setTransactionLimit(new BigDecimal(1000));
                account4.setCreatedAt(LocalDateTime.now());
                account4.setVerifiedBy(userMapper.toModel(savedAdmin));
                accountRepository.save(account4);

                Account ATMAccount = new Account();
                ATMAccount.setIban(IBANGenerator.generateIBAN());
                ATMAccount.setBalance(new BigDecimal("10000.00"));
                ATMAccount.setUser(userMapper.toModel(savedATM));
                ATMAccount.setAccountType(AccountType.CHECKING);
                ATMAccount.setAbsoluteLimit(new BigDecimal(0));
                ATMAccount.setTransactionLimit(new BigDecimal(2000));
                ATMAccount.setCreatedAt(LocalDateTime.now());
                ATMAccount.setVerifiedBy(userMapper.toModel(savedAdmin));
                accountRepository.save(ATMAccount);

                // make an test Transaction
                Transaction transaction = new Transaction();
                transaction.setAmount(new BigDecimal("250.00"));
                transaction.setCreatedAt(LocalDateTime.now());
                transaction.setFromAccount(account3);
                transaction.setInitiator(userMapper.toModel(savedAdmin));
                transaction.setToAccount(account4);
                transaction.setDescription("Test transaction from Alice to Bob");

                // Assuming you have a TransactionRepository and it's injected similarly
                transactionRepository.save(transaction);

        }
}