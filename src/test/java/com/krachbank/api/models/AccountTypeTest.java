package com.krachbank.api.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountTypeTest {

    @Test
    void testFromStringWithCheckingLowerCase() {
        assertEquals(AccountType.CHECKING, AccountType.fromString("checking"));
    }

    @Test
    void testFromStringWithCheckingUpperCase() {
        assertEquals(AccountType.CHECKING, AccountType.fromString("CHECKING"));
    }

    @Test
    void testFromStringWithCheckingMixedCase() {
        assertEquals(AccountType.CHECKING, AccountType.fromString("ChEcKiNg"));
    }

    @Test
    void testFromStringWithSavingsLowerCase() {
        assertEquals(AccountType.SAVINGS, AccountType.fromString("savings"));
    }

    @Test
    void testFromStringWithSavingsUpperCase() {
        assertEquals(AccountType.SAVINGS, AccountType.fromString("SAVINGS"));
    }

    @Test
    void testFromStringWithSavingsMixedCase() {
        assertEquals(AccountType.SAVINGS, AccountType.fromString("SaViNgS"));
    }

    @Test
    void testFromStringWithInvalidTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> AccountType.fromString("invalid"));
    }

    @Test
    void testFromStringWithNullThrowsException() {
        assertThrows(NullPointerException.class, () -> AccountType.fromString(null));
    }
}