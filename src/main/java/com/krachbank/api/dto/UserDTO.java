package com.krachbank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements DTO {

    private Long id;
    private BigDecimal transferLimit;
    private LocalDateTime createdAt;
    private boolean isVerified;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private int BSN;





}
