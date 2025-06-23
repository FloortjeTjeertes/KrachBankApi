package com.krachbank.api.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.krachbank.api.models.AccountType;



class AccountFilterTest {

    @Test
    void testIbanGetterSetter() {
        AccountFilter filter = new AccountFilter();
        filter.setIban("DE1234567890");
        assertEquals("DE1234567890", filter.getIban());
    }

    @Test
    void testBalanceMinGetterSetter() {
        AccountFilter filter = new AccountFilter();
        BigDecimal min = new BigDecimal("100.00");
        filter.setBalanceMin(min);
        assertEquals(min, filter.getBalanceMin());
    }

    @Test
    void testBalanceMaxGetterSetter() {
        AccountFilter filter = new AccountFilter();
        BigDecimal max = new BigDecimal("1000.00");
        filter.setBalanceMax(max);
        assertEquals(max, filter.getBalanceMax());
    }

    @Test
    void testUserIdGetterSetter() {
        AccountFilter filter = new AccountFilter();
        filter.setUserId(42L);
        assertEquals(42L, filter.getUserId());
    }

    @Test
    void testAccountTypeGetterSetter() {
        AccountFilter filter = new AccountFilter();
        filter.setAccountType("SAVINGS");
        // Assuming AccountType.fromString("SAVINGS") returns AccountType.SAVINGS
        assertEquals(AccountType.fromString("SAVINGS"), filter.getAccountType());
    }

    @Test
    void testAccountTypeNullOrEmpty() {
        AccountFilter filter = new AccountFilter();
        filter.setAccountType(null);
        assertNull(filter.getAccountType());

        filter.setAccountType("");
        assertNull(filter.getAccountType());
    }
}
