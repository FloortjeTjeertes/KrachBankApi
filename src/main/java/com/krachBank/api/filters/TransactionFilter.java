package com.krachbank.api.filters;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionFilter extends baseFilter {
    private Long senderId;
    private Long receiverId;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDateTime beforeDate;
    private LocalDateTime afterDate;


    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public LocalDateTime getBeforeDate() {
        return beforeDate;
    }

    public void setBeforeDate(LocalDateTime beforeDate) {
        this.beforeDate = beforeDate;
    }

    public LocalDateTime getAfterDate() {
        return afterDate;
    }

    public void setAfterDate(LocalDateTime afterDate) {
        this.afterDate = afterDate;
    }

   

}
