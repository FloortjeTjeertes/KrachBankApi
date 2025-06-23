package com.krachbank.api.mappers;

import com.krachbank.api.dto.AccountDTORequest;
import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.User;

import org.iban4j.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;





class AccountMapperTest {

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();
    }

    @Test
    void toModel_shouldMapDtoToModel() {
        AccountDTORequest dto = new AccountDTORequest();
        dto.setAccountType(AccountType.SAVINGS);
        dto.setBalance(BigDecimal.valueOf(1000));
        dto.setAbsoluteLimit(BigDecimal.valueOf(-100));
        dto.setTransactionLimit(BigDecimal.valueOf(500));

        Account account = accountMapper.toModel(dto);

        assertNotNull(account);
        assertNotNull(account.getIban());
        assertEquals(dto.getAccountType(), account.getAccountType());
        assertEquals(dto.getBalance(), account.getBalance());
        assertEquals(dto.getAbsoluteLimit(), account.getAbsoluteLimit());
        assertEquals(dto.getTransactionLimit(), account.getTransactionLimit());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void toModel_shouldThrowExceptionWhenDtoIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountMapper.toModel(null);
        });
        assertEquals("AccountDTOResponse cannot be null", exception.getMessage());
    }

    @Test
    void toResponse_shouldMapModelToDto() {
        Account account = new Account();
        account.setIban(Iban.valueOf("DE32500211205487556354")); 
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(BigDecimal.valueOf(2000));
        account.setAbsoluteLimit(BigDecimal.valueOf(-200));
        account.setTransactionLimit(BigDecimal.valueOf(1000));
        account.setCreatedAt(LocalDateTime.of(2024, 6, 1, 12, 0));

        User user = new User();
        user.setId(42L);
        account.setUser(user);

        AccountDTOResponse dto = accountMapper.toResponse(account);

        assertNotNull(dto);
        assertEquals(account.getIban().toString(), dto.getIban());
        assertEquals(account.getBalance(), dto.getBalance());
        assertEquals(user.getId(), dto.getOwner());
        assertEquals(account.getAbsoluteLimit(), dto.getAbsoluteLimit());
        assertEquals(account.getTransactionLimit(), dto.getTransactionLimit());
        assertEquals(account.getAccountType(), dto.getType());
        assertEquals(account.getCreatedAt().toString(), dto.getCreatedAt());
    }

    @Test
    void toResponse_shouldReturnUnknownWhenCreatedAtIsNull() {
        Account account = new Account();
        account.setIban(Iban.valueOf("DE32500211205487556354"));
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(BigDecimal.valueOf(2000));
        account.setAbsoluteLimit(BigDecimal.valueOf(-200));
        account.setTransactionLimit(BigDecimal.valueOf(1000));
        account.setCreatedAt(null);

        User user = new User();
        user.setId(42L);
        account.setUser(user);

        AccountDTOResponse dto = accountMapper.toResponse(account);

        assertEquals("Unknown", dto.getCreatedAt());
    }

    @Test
    void toResponse_shouldThrowExceptionWhenModelIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountMapper.toResponse(null);
        });
        assertEquals("Account cannot be null", exception.getMessage());
    }
}