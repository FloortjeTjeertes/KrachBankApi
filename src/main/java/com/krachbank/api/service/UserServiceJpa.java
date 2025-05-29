// src/main/java/com/krachbank/api/service/UserServiceJpa.java
package com.krachbank.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;

import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserServiceJpa implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceJpa(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> getUsers() {
        // USE UserDTO.fromModel() for consistent conversion
        return userRepository.findAll().stream()
                .map(UserDTO::fromModel) // <--- CHANGED HERE
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        // USE UserDTO.fromModel() for consistent conversion
        return UserDTO.fromModel(user); // <--- CHANGED HERE
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
        // Note: Your User model has getBSN() returning String, but your method here parses to int
        if (user.getBSN() == null || user.getBSN().isEmpty()) {
            throw new IllegalArgumentException("BSN is required");
        }
        try {
            if (Integer.parseInt(user.getBSN()) <= 0) {
                throw new IllegalArgumentException("BSN must be a positive number");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("BSN must be a valid number", e);
        }

        // Assuming toDTO() on your User model correctly converts to a DTO (e.g., UserDTO)
        return userRepository.save(user).toDTO();
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // --- Validation for existing user (based on email and username) ---
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + userDTO.getEmail() + " already exists!");
        }
        // This check is important if username is "First Last" and needs to be unique.
        // It will throw if a user with that exact first and last name combination already exists.
        if (userDTO.getUsername() != null && userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("User with username " + userDTO.getUsername() + " already exists!");
        }

        // --- Convert UserDTO to User entity ---
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setBSN(userDTO.getBSN());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setUsername(userDTO.getUsername()); // Set the username (e.g., "First Last")

        // --- Encode the password ---
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // --- Set default properties for the new user ---
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setVerified(false);
        user.setDailyLimit(0.0);
        // user.setTransferLimit(0.0); // Set a default transfer limit, if you have this field in User entity

        // --- Save the User entity to the database ---
        User savedUser = userRepository.save(user);

        // --- Debugging Print (can remove after successful testing) ---
        System.out.println("User created successfully: " + savedUser.getEmail() + " (Username: " + savedUser.getUsername() + ")");

        // --- Convert the saved User entity back to UserDTO for response ---
        return UserDTO.fromModel(savedUser);
    }

    @Override
    public UserDTO updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        // Update the fields of the existing user with new details
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        existingUser.setDailyLimit(userDetails.getDailyLimit());
        existingUser.setBSN(userDetails.getBSN()); // Ensure this maps correctly from userDetails
        existingUser.setVerified(userDetails.isVerified());
        existingUser.setActive(userDetails.isActive());
        existingUser.setUsername(userDetails.getUsername()); // <--- Ensure username is updated if passed

        // Save the updated entity
        User updatedUser = userRepository.save(existingUser);

        // USE UserDTO.fromModel() for consistent conversion
        return UserDTO.fromModel(updatedUser); // <--- CHANGED HERE
    }

    @Override
    public UserDTO deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        user.setActive(false);
        User deactivatedUser = userRepository.save(user);
        // USE UserDTO.fromModel() for consistent conversion
        return UserDTO.fromModel(deactivatedUser); // <--- CHANGED HERE
    }

    @Override
    public List<UserDTO> getAllUsers(Map<String, String> params) {
        // Implement logic to filter users based on parameters
        return getUsers();
    }
}