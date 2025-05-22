package com.krachbank.api.service;

import java.util.List;
import java.util.Map;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;

import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

public interface UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<UserResponse> getAllUsers(Map<String, String> filters) {
        // Filter implementation placeholder (can add JPA Specs or manual filtering)
        return userRepository.findAll().stream()
            .map(user -> modelMapper.map(user, UserResponse.class))
            .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return modelMapper.map(user, UserResponse.class);
    }

    public UserResponse createUser(UserRequest request) {
        User user = modelMapper.map(request, User.class);
        user.setIsActive(true);
        user.setIsVerified(false);
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        modelMapper.map(request, user);
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        user.setIsActive(false);
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    public UserResponse verifyUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        user.setIsVerified(true);
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

}
