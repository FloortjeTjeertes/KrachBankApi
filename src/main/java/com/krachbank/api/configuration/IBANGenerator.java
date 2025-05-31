package com.krachBank.api.configuration;

import java.util.Random;

import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class IBANGenerator {
    private static final String BANK_CODE = "KRCH";

    public static Iban generateIBAN() {
        // Generate a 10-digit account number for NL IBAN
        String accountNumber = String.format("%010d", new Random().nextLong() % 1_000_000_0000L);
        if (accountNumber.startsWith("-")) {
            accountNumber = accountNumber.substring(1);
        }
        accountNumber = String.format("%010d", Long.parseLong(accountNumber));
        return new Iban.Builder()
                .countryCode(CountryCode.NL)
                .bankCode(BANK_CODE)
                .accountNumber(accountNumber)
                .build();
    }

 

}
