package com.krachbank.api.dto;

import java.time.LocalDateTime;

import com.krachbank.api.models.Model;
import com.krachbank.api.models.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements DTO {

    private Long id;
    private String transferLimit;
    private LocalDateTime createdAt;
    private boolean isVerified;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private int BSN;

    @Override
    public User ToModel() {
        User user = new User();
        user.setId(this.id);
        user.setDailyLimit(this.transferLimit);
        user.setCreatedAt(this.createdAt);
        user.setVerified(this.isVerified);
        user.setActive(this.isActive);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setPhoneNumber(this.phoneNumber);
        user.setBsn(this.BSN);
        return user;
    }
    public static UserDTO fromModel(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setVerified(user.isVerified());
        dto.setActive(user.isActive());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setBSN(user.getBsn());
        return dto;
    }

}
