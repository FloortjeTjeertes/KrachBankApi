package com.krachbank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO  implements DTO {
    private BigDecimal amount;
    private String receiver;
    private String sender;
    private String description;
    private Long initiator;
    private LocalDateTime createdAt;




}
