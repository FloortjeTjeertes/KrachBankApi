// src/main/java/com/krachbank/api/service/UserServiceJpa.java
package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;

@Service
public class UserServiceJpa implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceJpa(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return toDTO(user);
    }

    @Override
    public DTO verifyUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        // Remove redundant null check for user
        if (user.getId() == null || !user.getId().equals(id)) {
            throw new IllegalArgumentException("User ID mismatch or user is null");
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
        String bsn = user.getBSN();
        if (bsn == null || bsn.isEmpty()) {
            throw new IllegalArgumentException("BSN is required");
        }
        if (bsn .length() != 9 || !bsn.matches("\\d+")) {
            throw new IllegalArgumentException("BSN must be a 9-digit number");
        }
        user.setVerified(true);

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // --- Validation for required fields ---
        if (userDTO.getPassword() == null) {
            throw new NullPointerException("Password is required");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userDTO.getFirstName() == null || userDTO.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (userDTO.getLastName() == null || userDTO.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        String bsn = userDTO.getBSN();
        if (bsn == null || bsn.isEmpty()) {
            throw new IllegalArgumentException("BSN is required");
        }
        if (bsn .length() != 9 || !bsn.matches("\\d+")) {
            throw new IllegalArgumentException("BSN must be a 9-digit number");
        }
        // --- Validation for existing user (based on email and username) ---
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + userDTO.getEmail() + " already exists!");
        }
        if (userDTO.getUsername() != null && userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("User with username " + userDTO.getUsername() + " already exists!");
        }

        // TODO: use fromModel in controller
        // --- Convert UserDTO to User entity ---
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setBSN(String.valueOf(userDTO.getBSN()));
        user.setPhoneNumber(userDTO.getPhoneNumber());

        // Set username from DTO or combine first and last name if not set
        if (userDTO.getUsername() != null && !userDTO.getUsername().isEmpty()) {
            user.setUsername(userDTO.getUsername());
        } else {
            user.setUsername(userDTO.getFirstName() + " " + userDTO.getLastName());
        }

        // --- Encode the password ---
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setVerified(userDTO.isVerified());
        user.setAdmin(userDTO.getIsAdmin() != null ? userDTO.getIsAdmin() : false); 

        // Set dailyLimit and transferLimit from DTO if present, else default to 0.0
        if (userDTO.getDailyLimit() != null) {
            user.setDailyLimit(userDTO.getDailyLimit());
        } else {
            user.setDailyLimit(BigDecimal.valueOf(0.0));
        }
        if (userDTO.getTransferLimit() != null) {
            user.setTransferLimit(userDTO.getTransferLimit());
        } else {
            user.setTransferLimit(BigDecimal.valueOf(0.0));
        }

        // --- Save the User entity to the database ---
        User savedUser = userRepository.save(user);
        return toDTO(savedUser);
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
        existingUser.setTransferLimit(userDetails.getTransferLimit());
        existingUser.setBSN(userDetails.getBSN()); // Ensure this maps correctly from userDetails
        existingUser.setVerified(userDetails.isVerified());
        existingUser.setActive(userDetails.isActive());
        User updatedUser = userRepository.save(existingUser);

        return toDTO(updatedUser); // <--- CHANGED HERE
    }

    @Override
    public UserDTO deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        user.setActive(false);
        User deactivatedUser = userRepository.save(user);
        // USE UserDTO.fromModel() for consistent conversion
        return toDTO(deactivatedUser); // <--- CHANGED HERE
    }

    @Override
    public List<UserDTO> getAllUsers(Map<String, String> params, UserFilter filter) {
        Specification<User> specification = makeUserFilterSpecification(params);
        Pageable pageable = filter != null ? filter.toPageAble() : Pageable.unpaged();
        Page<User> users;

        try {
            if (specification == null) {
                // No filter params: return all users
                users = userRepository.findAll(pageable);
            } else {
                // Filter params present: return filtered users
                users = userRepository.findAll(specification, pageable);
            }
        } catch (Exception e) {
            users = null;
        }
        if (users == null) {
            return new ArrayList<>();
        }
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // @Override
    // public List<UserDTO> getUsers() {
    // List<User> users = userRepository.findAll();
    // return users.stream().map(this::toDTO).collect(Collectors.toList());
    // }

    public Specification<User> makeUserFilterSpecification(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            // Return null if params is null or empty, as expected by the test
            return null;
        }
        return (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            if (params.containsKey("email")) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + params.get("email").toLowerCase() + "%"));
            }
            if (params.containsKey("firstName")) {
                predicates.add(
                        cb.like(cb.lower(root.get("firstName")), "%" + params.get("firstName").toLowerCase() + "%"));
            }
            if (params.containsKey("createdBefore")) {
                LocalDateTime createBefore = LocalDateTime.parse(params.get("createdBefore"));
                predicates.add(cb.lessThan(root.get("createdAt"), createBefore));
            }
            if (params.containsKey("createdAfter")) {
                LocalDateTime createAfter = LocalDateTime.parse(params.get("createdAfter"));
                predicates.add(cb.greaterThan(root.get("createdAt"), createAfter));
            }
            if (params.containsKey("lastName")) {
                predicates
                        .add(cb.like(cb.lower(root.get("lastName")), "%" + params.get("lastName").toLowerCase() + "%"));
            }
            if (params.containsKey("active")) {
                boolean active = Boolean.parseBoolean(params.get("active"));
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (params.containsKey("verified")) {
                boolean verified = Boolean.parseBoolean(params.get("verified"));
                predicates.add(cb.equal(root.get("verified"), verified));
            }
            // Add more filters as needed
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setBSN(user.getBSN());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword()); // Note: Password should not be exposed in DTOs, consider removing this
        dto.setActive(user.isActive());
        dto.setVerified(user.isVerified());
        dto.setDailyLimit(user.getDailyLimit());
        dto.setTransferLimit(user.getTransferLimit());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setIsAdmin(user.isAdmin());
        return dto;
    }

    @Override
    public List<UserDTO> toDTO(List<User> users) {
        if (users == null) {
            return new ArrayList<>();
        }
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            if (user != null) {
                userDTOs.add(toDTO(user));
            }
        }
        return userDTOs;
    }

}
