package com.krachbank.api.mappers;

import com.krachbank.api.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class UserMapper extends BaseMapper<User, UserDTO, UserDTO> {
    private final PasswordEncoder passwordEncoder;

    public UserMapper() {
        this.passwordEncoder = null;
    }

    @Override
    public User toModel(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setDailyLimit(dto.getDailyLimit());
        user.setCreatedAt(dto.getCreatedAt());
        user.setVerified(dto.isVerified());
        user.setActive(dto.isActive());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setBSN(dto.getBSN());
        user.setAdmin(dto.getIsAdmin() != null ? dto.getIsAdmin() : false); // Fix here
        return user;
    }
    public User fromRegisterRequest(RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBSN(request.getBSN());
        user.setCreatedAt(LocalDateTime.now());
        user.setVerified(false);
        user.setActive(true);
        user.setDailyLimit(BigDecimal.ZERO);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return user;
    }
    @Override
    public UserDTO toResponse(User model) {
        if (model == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        UserDTO dto = new UserDTO();
        dto.setId(model.getId());
        dto.setUsername(model.getEmail());
        dto.setEmail(model.getEmail());
        return dto;
    }

}
