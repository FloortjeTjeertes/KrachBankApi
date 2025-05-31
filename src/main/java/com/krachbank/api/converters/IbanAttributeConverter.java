package com.krachbank.api.converters; // You might want to create a 'converter' package

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;

@Converter(autoApply = true) // autoApply makes this converter automatically apply to all Iban fields
public class IbanAttributeConverter implements AttributeConverter<Iban, String> {

    @Override
    public String convertToDatabaseColumn(Iban iban) {
        if (iban == null) {
            return null;
        }
        return iban.toString();
    }

    @Override
    public Iban convertToEntityAttribute(String ibanString) {
        if (ibanString == null || ibanString.trim().isEmpty()) {
            return null;
        }
        try {
            return Iban.valueOf(ibanString);
        } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
            // Handle parsing errors, e.g., log them or throw a more specific exception
            throw new IllegalArgumentException("Invalid IBAN string: " + ibanString, e);
        }
    }
}