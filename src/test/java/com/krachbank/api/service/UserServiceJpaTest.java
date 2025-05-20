package com.krachbank.api.service;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceJpaTest {
    private UserServiceJpa userService;
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceJpa(userRepository);

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
        user1.setBsn(123456789);

        user2 = new User();
        user2.setId(2L);
        user2.setDailyLimit(BigDecimal.valueOf( 2000));
        user2.setCreatedAt(LocalDateTime.now());
        user2.setVerified(false);
        user2.setActive(false);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@example.com");
        user2.setPhoneNumber("0987654321");
        user2.setBsn(987654321);
    }

    @Test
    void testGetUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserDTO> users = userService.getUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(user1.getId(), users.get(0).getId());
        assertEquals(user2.getId(), users.get(1).getId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testVerifyUser() {
        user1.setVerified(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Simulate that the repository sets verified to true (if your service does
            // this)
            savedUser.setVerified(true);
            return savedUser;
        });

        UserDTO dto = (UserDTO) userService.verifyUser(user1);

        assertNotNull(dto);
        assertEquals(user1.getId(), dto.getId());
        assertEquals(user1.getEmail(), dto.getEmail());
        assertTrue(user1.isVerified(), "User should be verified after verifyUser");
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void testVerifyUser_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(null));
    }

    @Test
    void testVerifyUser_MissingEmail_ThrowsException() {
        user1.setEmail(null);
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
        user1.setEmail(""); // empty string
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
    }

    @Test
    void testVerifyUser_MissingFirstName_ThrowsException() {
        user1.setFirstName(null);
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
        user1.setFirstName(""); // empty string
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
    }

    @Test
    void testVerifyUser_MissingLastName_ThrowsException() {
        user1.setLastName(null);
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
        user1.setLastName(""); // empty string
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
    }

    @Test
    void testVerifyUser_InvalidBsn_ThrowsException() {
        user1.setBsn(0);
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
        user1.setBsn(-1);
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1));
    }
}
