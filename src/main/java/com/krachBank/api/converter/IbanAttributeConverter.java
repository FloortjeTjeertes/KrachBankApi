package com.krachBank.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.iban4j.Iban;

@Converter(autoApply = true)
public class IbanAttributeConverter implements AttributeConverter<Iban, String> {
    @Override
    public String convertToDatabaseColumn(Iban iban) {
        return iban != null ? iban.toString() : null;
    }

    @Override
    public Iban convertToEntityAttribute(String dbData) {
        return dbData != null ? Iban.valueOf(dbData) : null;
    }
}