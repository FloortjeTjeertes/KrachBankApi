package com.krachBank.api.models;

public enum AccountType {
    CHECKING,
    SAVINGS;

    public static AccountType fromString(String type) {
        return AccountType.valueOf(type.toUpperCase());
    }
}
