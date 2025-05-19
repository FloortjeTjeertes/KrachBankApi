package com.krachbank.api.converters;

import org.iban4j.Iban;

import jakarta.persistence.Converter;

@Converter(autoApply = true)    
public class IbanConverter implements jakarta.persistence.AttributeConverter<Iban, String> {
     @Override
    public String convertToDatabaseColumn(Iban iban) {
        return iban != null ? iban.toString() : null;
    }
    @Override
    public Iban convertToEntityAttribute(String dbData) {
        return dbData != null ? Iban.valueOf(dbData) : null;
    }
}
