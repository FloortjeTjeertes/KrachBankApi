package com.krachBank.api.service;

import com.krachBank.api.dto.UserDTO;
import com.krachBank.api.models.User;
import com.krachBank.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceJPA implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Configuration
    public static class ModelMapperConfig {
        @Bean
        public ModelMapper modelMapper() {
            return new ModelMapper();
        }
    }

    @Override
    public List<UserDTO> getAllUsers(Map<String, String> filters) {
        log.info("Fetching all users with filters: {}", filters);
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        validateId(id);
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null) throw new IllegalArgumentException("UserDTO cannot be null");
        log.info("Creating new user: {}", userDTO.getEmail());
        User user = modelMapper.map(userDTO, User.class);
        user.setIsActive(true);
        user.setIsVerified(false);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        validateId(id);
        if (userDTO == null) throw new IllegalArgumentException("UserDTO cannot be null");
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        modelMapper.map(userDTO, user);
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public UserDTO deactivateUser(Long id) {
        validateId(id);
        log.info("Deactivating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setIsActive(false);
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public UserDTO verifyUser(Long id) {
        validateId(id);
        log.info("Verifying user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setIsVerified(true);
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid user ID: " + id);
    }
    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(Long id) {
            super("User not found with ID: " + id);
        }
    }
    //todo delete user
}