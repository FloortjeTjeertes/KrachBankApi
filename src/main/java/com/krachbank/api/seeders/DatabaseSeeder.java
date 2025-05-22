package com.krachbank.api.seeders;

import com.krachbank.api.models.Account;
import com.krachbank.api.models.User;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.repository.AccountRepository;
import com.krachbank.api.repository.UserRepository;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public DatabaseSeeder(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @PostConstruct
    public void seed() {
        // Create users
        // TODO: passwords need to be hashed acording to the security and authentication methods
        User user1 = new User();
        user1.setFirstName("Alice");
        user1.setLastName("Smith");
        user1.setNickName("Alice Smith");
        user1.setDailyLimit(new BigDecimal("1000.00"));
        user1.setEmail("alice@example.com");
        user1.setPassword("password123");
        user1.setPhoneNumber("+491234567890");
        user1.setCreatedAt(LocalDateTime.now());
        userRepository.save(user1);

        User user2 = new User();
        user2.setFirstName("Bob");
        user2.setLastName("Johnson");
        user2.setNickName("Bob Johnson");
        user2.setDailyLimit(new BigDecimal("500.00"));
        user2.setEmail("bob@example.com");
        user2.setPassword("password123");
        user2.setPhoneNumber("+491234567891");
        user2.setCreatedAt(LocalDateTime.now());
        userRepository.save(user2);

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setNickName("Admin User");
        admin.setDailyLimit(new BigDecimal("200.00"));
        admin.setEmail("admin@example.com");
        admin.setPassword("adminpass");
        admin.setPhoneNumber("+491234567892");
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        // Create accounts
        Account account1 = new Account();
        account1.setAbsoluteLimit(new BigDecimal(-100));
        account1.setAccountType(AccountType.CHECKING);
        account1.setBalance(new BigDecimal("5000.00"));
        account1.setTransactionLimit(new BigDecimal(1000));
        account1.setCreatedAt(LocalDateTime.now());
        account1.setUser(user1);
        account1.setVerifiedBy(user2);
        account1.setIBAN(Iban.valueOf("DE89370400440532013000"));
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setIBAN(Iban.valueOf("DE12500105170648489890"));
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setUser(user2);
        account2.setAccountType(AccountType.SAVINGS);
        account2.setAbsoluteLimit(new BigDecimal(-100));
        account2.setTransactionLimit(new BigDecimal(1000));
        account2.setCreatedAt(LocalDateTime.now());
        account2.setVerifiedBy(user1);
        accountRepository.save(account2);

        // Add more seeding as needed
    }
}