package com.krachbank.api.converters;

import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IbanConverterTest {

    private final IbanConverter converter = new IbanConverter();

    @Test
    void testConvertToDatabaseColumn_withValidIban() {
        Iban iban = Iban.valueOf("DE89370400440532013000");
        String dbValue = converter.convertToDatabaseColumn(iban);
        assertEquals("DE89370400440532013000", dbValue);
    }

    @Test
    void testConvertToDatabaseColumn_withNull() {
        String dbValue = converter.convertToDatabaseColumn(null);
        assertNull(dbValue);
    }

    @Test
    void testConvertToEntityAttribute_withValidString() {
        String dbData = "DE89370400440532013000";
        Iban iban = converter.convertToEntityAttribute(dbData);
        assertNotNull(iban);
        assertEquals(dbData, iban.toString());
    }

    @Test
    void testConvertToEntityAttribute_withNull() {
        Iban iban = converter.convertToEntityAttribute(null);
        assertNull(iban);
    }

    @Test
    void testConvertToEntityAttribute_withInvalidString() {
        String invalidDbData = "NL91ABNA041716430";
        assertThrows(IbanFormatException.class, () -> converter.convertToEntityAttribute(invalidDbData));
    }
}