package com.krachbank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO  implements DTO {
    private double amount;
    private String receiver;
    private String sender;
    private String description;
    private Long initiator;
    private String createdAt;




}
