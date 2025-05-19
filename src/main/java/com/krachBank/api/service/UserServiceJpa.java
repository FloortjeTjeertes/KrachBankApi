package com.krachbank.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;

import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

@Service
public class UserServiceJpa implements UserService {
    private final UserRepository userRepository;

    public UserServiceJpa(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        String.valueOf(user.getDailyLimit()),
                        user.getCreatedAt(),
                        user.isVerified(),
                        user.isActive(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getBsn()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserById'");
    }

    @Override
    public DTO verifyUser(User user) {
        // Basic validation example, adjust as needed for your User fields
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (user.getBsn() <= 0) {
            throw new IllegalArgumentException("BSN must be a positive number");
        }
        return userRepository.save(user).toDTO();
    }

    @Override
    public UserDTO updateUser(Long id, User userDTO) {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void removeUser(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeUser'");
    }

    @Override
    public User toModel(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setDailyLimit(dto.getTransferLimit());
        user.setCreatedAt(dto.getCreatedAt());
        user.setVerified(dto.isVerified());
        user.setActive(dto.isActive());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setBsn(dto.getBSN());
        return user;
    }

    @Override
    public UserDTO toDTO(User model) {
        return model.toDTO();
    }

    @Override
    public List<UserDTO> toDTO(List<User> users) {

        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            userDTOs.add(toDTO(user));
        }
        return userDTOs;
    }

}
