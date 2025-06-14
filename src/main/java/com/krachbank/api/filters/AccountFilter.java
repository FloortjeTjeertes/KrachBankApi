package com.krachbank.api.filters;

import java.math.BigDecimal;

import com.krachbank.api.models.AccountType;

public class AccountFilter extends BaseFilter {
    private String iban;
    private BigDecimal balanceMin;
    private BigDecimal balanceMax;
    private String accountType;
    private Long userId;
  
    
    public String getIban() {
        return iban;
    }
    public void setIban(String iBAN) {
        iban = iBAN;
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
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    


    public AccountType getAccountType() {
        if (accountType == null || accountType.isEmpty()) {
            return null;
        }
        return AccountType.fromString(accountType);
    }
    public void setAccountType(String Type) {
        accountType = Type;
    }


}
