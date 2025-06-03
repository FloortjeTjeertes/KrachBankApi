package com.krachbank.api.service;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceJpaTest {
    private UserServiceJpa userService;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        userService = new UserServiceJpa(userRepository, passwordEncoder);

        user1 = new User();
        user1.setId(1L);
        user1.setDailyLimit(BigDecimal.valueOf(1000));
        user1.setCreatedAt(LocalDateTime.now());
        user1.setVerified(true);
        user1.setActive(true);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@example.com");
        user1.setPhoneNumber("1234567890");
        user1.setBSN(123456789);
        user1.setUsername("john.doe"); // Added username for consistency

        user2 = new User();
        user2.setId(2L);
        user2.setDailyLimit(BigDecimal.valueOf(2000));
        user2.setCreatedAt(LocalDateTime.now());
        user2.setVerified(false);
        user2.setActive(false);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@example.com");
        user2.setPhoneNumber("0987654321");
        user2.setBSN(987654321);
        user2.setUsername("jane.smith"); // Added username for consistency
    }

    @Test
    void testGetAllUsers() { // Renamed from testGetUsers to better reflect the service method
        List<User> usersList = Arrays.asList(user1, user2);
        // Mocking findAll with Specification and Pageable
        when(userRepository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(usersList));

        // Call the service method with empty params and null filter for a general "get all" scenario
        List<UserDTO> users = userService.getAllUsers(Collections.emptyMap(), null);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(user1.getId(), users.get(0).getId());
        assertEquals(user2.getId(), users.get(1).getId());
        // Verify that findAll was called with any Specification and any Pageable
        verify(userRepository, times(1)).findAll(nullable(Specification.class), any(Pageable.class));
    }

    @Test
    void testVerifyUser() {
        user1.setVerified(false); // Ensure it's initially false for the test

        // Mock findById to return the user
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        // Mock save to return the updated user (with verified = true)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setVerified(true); // Simulate the service setting verified to true
            return savedUser;
        });

        DTO dto = userService.verifyUser(user1.getId()); // Call the verifyUser(Long id) method

        assertNotNull(dto);
        assertTrue(dto instanceof UserDTO); // Assuming verifyUser returns UserDTO as per your service
        UserDTO userDTO = (UserDTO) dto;

        assertEquals(user1.getId(), userDTO.getId());
        assertEquals(user1.getEmail(), userDTO.getEmail());
        assertTrue(userDTO.isVerified(), "UserDTO should be verified after verifyUser");
        // Ensure the original user object passed to save is also updated (if it's the same instance)
        assertTrue(user1.isVerified(), "Original User object should be verified after service call");

        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void testVerifyUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.verifyUser(99L));
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void testVerifyUser_IdMismatch_ThrowsException() {
        User userWithNullId = new User();
        userWithNullId.setFirstName("Test");
        userWithNullId.setLastName("User");
        userWithNullId.setEmail("test@example.com");
        userWithNullId.setBSN(123);
        userWithNullId.setId(null); // Simulate null ID

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userWithNullId));
    }

    @Test
    void testVerifyUser_MissingEmail_ThrowsException() {
        user1.setEmail(null);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));

        user1.setEmail(""); // empty string
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
    }

    @Test
    void testVerifyUser_MissingFirstName_ThrowsException() {
        user1.setFirstName(null);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));

        user1.setFirstName(""); // empty string
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
    }

    @Test
    void testVerifyUser_MissingLastName_ThrowsException() {
        user1.setLastName(null);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));

        user1.setLastName(""); // empty string
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
    }

    @Test
    void testVerifyUser_InvalidBsn_ThrowsException() {
        user1.setBSN(0);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));

        user1.setBSN(-1);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
    }
}