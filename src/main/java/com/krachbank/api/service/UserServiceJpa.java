package com.krachbank.api.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;

import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException; // You might want to use a specific exception

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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return new UserDTO(
                user.getId(),
                String.valueOf(user.getDailyLimit()),
                user.getCreatedAt(),
                user.isVerified(),
                user.isActive(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBsn());
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
        if (user.getBsn() == null || user.getBsn().isEmpty() || Integer.parseInt(user.getBsn()) <= 0) {
            throw new IllegalArgumentException("BSN must be a positive number");
        }
        return userRepository.save(user).toDTO();
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // You would typically convert the UserDTO to a User entity here,
        // then save it using userRepository.save(), and convert the result back to UserDTO.
        // For example:
        User user = new User();
        // Set properties from userDTO to user entity
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setBSN(userDTO.getBSN());
        user.setVerified(userDTO.isVerified());
        user.setActive(userDTO.isActive());
        // createdAt might be set by @CreationTimestamp or in service
        // user.setCreatedAt(LocalDateTime.now()); // Or use an annotation like @CreationTimestamp in your User entity

        User savedUser = userRepository.save(user);
        return new UserDTO(
                savedUser.getId(),
                String.valueOf(savedUser.getDailyLimit()),
                savedUser.getCreatedAt(),
                savedUser.isVerified(),
                savedUser.isActive(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getBsn());
    }

    @Override
    public UserDTO updateUser(Long id, User userDetails) {
        // Find the existing user
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        // Update the fields of the existing user with new details
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        existingUser.setDailyLimit(userDetails.getDailyLimit());
        existingUser.setBSN(userDetails.getBsn());
        existingUser.setVerified(userDetails.isVerified());
        existingUser.setActive(userDetails.isActive());
        // Do not update ID or createdAt unless it's a specific requirement for audit purposes

        // Save the updated entity. JpaRepository's save() handles the update when the ID is present.
        User updatedUser = userRepository.save(existingUser);

        // Convert the updated entity back to DTO
        return new UserDTO(
                updatedUser.getId(),
                String.valueOf(updatedUser.getDailyLimit()),
                updatedUser.getCreatedAt(),
                updatedUser.isVerified(),
                updatedUser.isActive(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getEmail(),
                updatedUser.getPhoneNumber(),
                updatedUser.getBsn());
    }

    @Override
    public UserDTO deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        user.setActive(false); // Set active to false
        User deactivatedUser = userRepository.save(user); // Save the updated user
        return new UserDTO(
                deactivatedUser.getId(),
                String.valueOf(deactivatedUser.getDailyLimit()),
                deactivatedUser.getCreatedAt(),
                deactivatedUser.isVerified(),
                deactivatedUser.isActive(),
                deactivatedUser.getFirstName(),
                deactivatedUser.getLastName(),
                deactivatedUser.getEmail(),
                deactivatedUser.getPhoneNumber(),
                deactivatedUser.getBsn());
    }

    @Override
    public List<UserDTO> getAllUsers(Map<String, String> params) {
        // Implement logic to filter users based on parameters
        // For simplicity, let's just return all users for now if no specific filtering logic is provided
        return getUsers(); // Re-use the existing getUsers method
    }
}