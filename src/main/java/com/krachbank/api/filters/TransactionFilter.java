package com.krachbank.api.filters;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionFilter extends BaseFilter {
    private String senderIban;
    private String receiverIban;
    private Long initiatorId;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String beforeDate;
    private String afterDate;

    public String getSenderIban() {
        return senderIban;
    }

    public void setSenderIban(String senderId) {
        this.senderIban = senderId;
    }

    public String getReceiverIban() {
        return receiverIban;
    }

    public void setReceiverIban(String receiverId) {
        this.receiverIban = receiverId;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
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
        return beforeDate != null ? LocalDateTime.parse(beforeDate) : null;
    }

    public void setBeforeDate(String beforeDate) {
        this.beforeDate = beforeDate;
    }

    public LocalDateTime getAfterDate() {
        return afterDate != null ? LocalDateTime.parse(afterDate) : null;
    }

    public void setAfterDate(String afterDate) {
        this.afterDate = afterDate;
    }

}
