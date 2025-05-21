package com.krachbank.api.filters;

public class AccountFilter {
    private String IBAN;
    private double balanceMin;
    private double balanceMax;
    private int limit;
    private int page;
    
    public String getIBAN() {
        return IBAN;
    }
    public void setIBAN(String iBAN) {
        IBAN = iBAN;
    }
    public double getBalanceMin() {
        return balanceMin;
    }
    public void setBalanceMin(double balanceMin) {
        this.balanceMin = balanceMin;
    }
    public double getBalanceMax() {
        return balanceMax;
    }
    public void setBalanceMax(double balanceMax) {
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

}
