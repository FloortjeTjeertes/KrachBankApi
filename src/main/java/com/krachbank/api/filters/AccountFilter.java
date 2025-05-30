package com.krachbank.api.filters;

import java.math.BigDecimal;

public class AccountFilter {
    private String IBAN;
    private BigDecimal balanceMin;
    private BigDecimal balanceMax;
    private String AccountType;
    private int limit;
    private int page;
    
    public String getIBAN() {
        return IBAN;
    }
    public void setIBAN(String iBAN) {
        IBAN = iBAN;
    }
    public BigDecimal getBalanceMin() {
        return balanceMin;
    }
    public void setBalanceMin(BigDecimal balanceMin) {
        this.balanceMin = balanceMin;
    }
    public BigDecimal getBalanceMax() {
        return balanceMax;
    }
    public void setBalanceMax(BigDecimal balanceMax) {
        this.balanceMax = balanceMax;
    }
    public int getLimit() {
        return limit;
    }
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }

    public String getAccountType() {
        return AccountType;
    }
    public void setAccountType(String accountType) {
        AccountType = accountType;
    }


}
