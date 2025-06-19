package com.krachbank.api.mappers;

import org.springframework.stereotype.Component;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;

@Component
public class UserMapper extends BaseMapper<User, UserDTO, UserDTO> {

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

    @Override
    public UserDTO toResponse(User model) {
        if (model == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        UserDTO dto = new UserDTO();
        dto.setId(model.getId());
        dto.setUsername(model.getUsername());
        dto.setEmail(model.getEmail());
        return dto;
    }

}
