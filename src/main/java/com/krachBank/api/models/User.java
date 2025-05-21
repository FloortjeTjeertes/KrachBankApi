package com.krachbank.api.models;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User implements Model {


    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String dailyLimit;

    private String password;

    private LocalDateTime createdAt;

    private boolean verified;

    private boolean active;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private int bsn;

    public DTO toDTO() {
        return new UserDTO(id, dailyLimit, createdAt, verified, active, firstName, lastName,
                email, phoneNumber, bsn);
    }

    public void setIsActive(boolean b) {
    }

    public void setIsVerified(boolean b) {
    }

}
