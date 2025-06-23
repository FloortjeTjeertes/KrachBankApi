package com.krachbank.api.converters;

import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IbanAttributeConverterTest {

    private final IbanAttributeConverter converter = new IbanAttributeConverter();

    @Test
    void testConvertToDatabaseColumn_withValidIban() {
        Iban iban = Iban.valueOf("DE89370400440532013000");
        String result = converter.convertToDatabaseColumn(iban);
        assertEquals("DE89370400440532013000", result);
    }

    @Test
    void testConvertToDatabaseColumn_withNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute_withValidString() {
        String ibanString = "DE89370400440532013000";
        Iban iban = converter.convertToEntityAttribute(ibanString);
        assertNotNull(iban);
        assertEquals(ibanString, iban.toString());
    }

    @Test
    void testConvertToEntityAttribute_withNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void testConvertToEntityAttribute_withEmptyString() {
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void testConvertToEntityAttribute_withInvalidIban() {
        String invalidIban = "INVALID_IBAN";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(invalidIban);
        });
        assertTrue(exception.getCause() instanceof IbanFormatException
                || exception.getCause() instanceof InvalidCheckDigitException
                || exception.getCause() instanceof UnsupportedCountryException);
    }
}