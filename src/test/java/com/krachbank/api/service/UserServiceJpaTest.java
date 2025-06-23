package com.krachbank.api.service;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceJpaTest {
    private UserServiceJpa userService;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    private User user1;
    private User user2;
    private UserDTO userDTO1;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        userService = new UserServiceJpa(userRepository, passwordEncoder);

        user1 = new User();
        user1.setId(1L);
        user1.setDailyLimit(BigDecimal.valueOf(1000));
        user1.setCreatedAt(LocalDateTime.of(2023, 1, 15, 10, 0)); // Specific date for filtering
        user1.setVerified(true);
        user1.setActive(true);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@example.com");
        user1.setPhoneNumber("1234567890");
        user1.setBSN(123456789);
        user1.setUsername("john.doe");

        user2 = new User();
        user2.setId(2L);
        user2.setDailyLimit(BigDecimal.valueOf(2000));
        user2.setCreatedAt(LocalDateTime.of(2024, 5, 20, 15, 30)); // Specific date for filtering
        user2.setVerified(false);
        user2.setActive(false);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@example.com");
        user2.setPhoneNumber("0987654321");
        user2.setBSN(987654321);
        user2.setUsername("jane.smith");

        userDTO1 = userService.toDTO(user1); // Pre-convert for convenience in tests
    }

    // --- getUserById Tests ---
    @Test
    @DisplayName("getUserById - Should return UserDTO when user exists")
    void getUserById_UserExists_ReturnsUserDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(user1.getId(), result.getId());
        assertEquals(user1.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUserById - Should throw EntityNotFoundException when user does not exist")
    void getUserById_UserDoesNotExist_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(99L));
        verify(userRepository, times(1)).findById(99L);
    }

    // --- createUser Tests ---
    @Test
    @DisplayName("createUser - Should create a new user successfully")
    void createUser_Success() {
        UserDTO newUserDTO = new UserDTO();
        newUserDTO.setFirstName("New");
        newUserDTO.setLastName("User");
        newUserDTO.setEmail("new.user@example.com");
        newUserDTO.setPhoneNumber("555-555-5555");
        newUserDTO.setBSN(111222333);
        newUserDTO.setUsername("new.user");
        newUserDTO.setPassword("rawPassword");

        when(userRepository.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(newUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3L); // Simulate ID generation by DB
            savedUser.setPassword("encodedPassword"); // Ensure encoded password is set
            return savedUser;
        });

        UserDTO createdUser = userService.createUser(newUserDTO);

        assertNotNull(createdUser.getId());
        assertEquals("New", createdUser.getFirstName());
        assertEquals("new.user@example.com", createdUser.getEmail());
        assertEquals("encodedPassword", createdUser.getPassword()); // Verifying encoded password in DTO
        assertTrue(createdUser.isActive());
        assertFalse(createdUser.isVerified());
        assertEquals(BigDecimal.valueOf(0.0), createdUser.getDailyLimit());
        assertNotNull(createdUser.getCreatedAt());

        verify(userRepository, times(1)).findByEmail(newUserDTO.getEmail());
        verify(userRepository, times(1)).findByUsername(newUserDTO.getUsername());
        verify(passwordEncoder, times(1)).encode(newUserDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Should throw RuntimeException if email already exists")
    void createUser_EmailAlreadyExists_ThrowsException() {
        UserDTO newUserDTO = new UserDTO();
        newUserDTO.setEmail("john@example.com"); // Existing email
        newUserDTO.setUsername("new.user"); // New username
        newUserDTO.setPassword("rawPassword");
        newUserDTO.setFirstName("New");
        newUserDTO.setLastName("User");
        newUserDTO.setPhoneNumber("555-555-5555");
        newUserDTO.setBSN(111222333);

        when(userRepository.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.of(user1));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.createUser(newUserDTO));
        assertEquals("User with email john@example.com already exists!", thrown.getMessage());

        verify(userRepository, times(1)).findByEmail(newUserDTO.getEmail());
        verify(userRepository, never()).findByUsername(anyString()); // Should not proceed to username check
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Should throw RuntimeException if username already exists")
    void createUser_UsernameAlreadyExists_ThrowsException() {
        UserDTO newUserDTO = new UserDTO();
        newUserDTO.setEmail("new.user@example.com");
        newUserDTO.setUsername("john.doe"); // Existing username
        newUserDTO.setPassword("rawPassword");
        newUserDTO.setFirstName("New");
        newUserDTO.setLastName("User");
        newUserDTO.setPhoneNumber("555-555-5555");
        newUserDTO.setBSN(111222333);

        when(userRepository.findByEmail(newUserDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(newUserDTO.getUsername())).thenReturn(Optional.of(user1));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.createUser(newUserDTO));
        assertEquals("User with username john.doe already exists!", thrown.getMessage());

        verify(userRepository, times(1)).findByEmail(newUserDTO.getEmail());
        verify(userRepository, times(1)).findByUsername(newUserDTO.getUsername());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- updateUser Tests ---
    @Test
    @DisplayName("updateUser - Should update an existing user successfully")
    void updateUser_Success() {
        User updatedDetails = new User();
        updatedDetails.setFirstName("Johnny");
        updatedDetails.setLastName("Depp");
        updatedDetails.setEmail("johnny.depp@example.com");
        updatedDetails.setPhoneNumber("111-222-3333");
        updatedDetails.setDailyLimit(BigDecimal.valueOf(1500));
        updatedDetails.setBSN(999888777);
        updatedDetails.setVerified(false); // Change status
        updatedDetails.setActive(false); // Change status
        updatedDetails.setUsername("johnny.depp");

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Simulate the save operation by returning the modified user
            return savedUser;
        });

        UserDTO result = userService.updateUser(user1.getId(), updatedDetails);

        assertNotNull(result);
        assertEquals(user1.getId(), result.getId()); // ID should remain the same
        assertEquals("Johnny", result.getFirstName());
        assertEquals("Depp", result.getLastName());
        assertEquals("johnny.depp@example.com", result.getEmail());
        assertEquals("111-222-3333", result.getPhoneNumber());
        assertEquals(BigDecimal.valueOf(1500), result.getDailyLimit());
        assertEquals(999888777, result.getBSN());
        assertFalse(result.isVerified());
        assertFalse(result.isActive());
        assertEquals("johnny.depp", result.getUsername());

        // Verify that the existing user object was updated
        assertEquals("Johnny", user1.getFirstName()); // user1 should be modified in this test
        assertEquals("johnny.depp", user1.getUsername());

        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).save(user1); // Verify save was called with the modified user1
    }

    @Test
    @DisplayName("updateUser - Should throw EntityNotFoundException when user does not exist")
    void updateUser_UserDoesNotExist_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        User userDetails = new User(); // Dummy user details for the call
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(99L, userDetails));
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    // --- deactivateUser Tests ---
    @Test
    @DisplayName("deactivateUser - Should deactivate a user successfully")
    void deactivateUser_Success() {
        user1.setActive(true); // Ensure user is active initially
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setActive(false); // Simulate the service setting active to false
            return savedUser;
        });

        UserDTO result = userService.deactivateUser(user1.getId());

        assertNotNull(result);
        assertEquals(user1.getId(), result.getId());
        assertFalse(result.isActive()); // Assert that the returned DTO shows deactivated
        assertFalse(user1.isActive()); // Assert that the original user object was also updated

        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    @DisplayName("deactivateUser - Should throw EntityNotFoundException when user does not exist")
    void deactivateUser_UserDoesNotExist_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deactivateUser(99L));
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Existing Tests (with minor improvements/fixes) ---

    @Test
    void testGetAllUsers() {
        List<User> usersList = Arrays.asList(user1, user2);
        // Mocking findAll with Specification and Pageable
        when(userRepository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(usersList));

        // Call the service method with empty params and null filter for a general "get
        // all" scenario
        List<UserDTO> users = userService.getAllUsers(Collections.emptyMap(), null);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(user1.getId(), users.get(0).getId());
        assertEquals(user2.getId(), users.get(1).getId());
        // Verify that findAll was called with any Specification and any Pageable
        verify(userRepository, times(1)).findAll(nullable(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getAllUsers - Should return filtered users by email")
    void getAllUsers_FilterByEmail_ReturnsFilteredList() {
        Map<String, String> params = new HashMap<>();
        params.put("email", "john@example.com");

        // Mock the findAll to return a Page containing only user1 when filtered by
        // email
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(user1)));

        List<UserDTO> users = userService.getAllUsers(params, null);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("john@example.com", users.get(0).getEmail());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getAllUsers - Should return filtered users by username")
    void getAllUsers_FilterByUsername_ReturnsFilteredList() {
        Map<String, String> params = new HashMap<>();
        // Note: Your makeUserFilterSpecification uses "userName" as key but then looks
        // for "firstName".
        // I'll test based on your current implementation, which is a potential bug/typo
        // in your spec builder.
        // Assuming you meant to use 'username' field in the future, if so, the filter
        // key should be "username".
        // For now, I'll test 'firstName' as per your existing
        // makeUserFilterSpecification.
        params.put("userName", "john"); // Testing with "userName" key to hit the "firstName" condition in spec

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(user1)));

        List<UserDTO> users = userService.getAllUsers(params, null);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getFirstName());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getAllUsers - Should return filtered users by creation date range")
    void getAllUsers_FilterByCreationDateRange_ReturnsFilteredList() {
        Map<String, String> params = new HashMap<>();
        params.put("createdAfter", "2023-01-01T00:00:00");
        params.put("createdBefore", "2024-01-01T00:00:00");

        // user1 (2023-01-15) fits, user2 (2024-05-20) does not
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(user1)));

        List<UserDTO> users = userService.getAllUsers(params, null);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("John", users.get(0).getFirstName());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getAllUsers - Should return filtered users by last name and active status")
    void getAllUsers_FilterByLastNameAndActive_ReturnsFilteredList() {
        Map<String, String> params = new HashMap<>();
        params.put("lastName", "smith");
        params.put("active", "false");

        // user2 (Smith, active=false) fits
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(user2)));

        List<UserDTO> users = userService.getAllUsers(params, null);

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("Jane", users.get(0).getFirstName());
        assertEquals("Smith", users.get(0).getLastName());
        assertFalse(users.get(0).isActive());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("testVerifyUser - Should verify user successfully")
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

        UserDTO dto = (UserDTO) userService.verifyUser(user1.getId());

        assertNotNull(dto);
        assertTrue(dto instanceof UserDTO);
        UserDTO userDTO = (UserDTO) dto;

        assertEquals(user1.getId(), userDTO.getId());
        assertEquals(user1.getEmail(), userDTO.getEmail());
        assertTrue(userDTO.isVerified(), "UserDTO should be verified after verifyUser");
        assertTrue(user1.isVerified(), "Original User object should be verified after service call");

        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    @DisplayName("testVerifyUser - User not found should throw EntityNotFoundException")
    void testVerifyUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.verifyUser(99L));
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("testVerifyUser - User with null ID should throw IllegalArgumentException")
    void testVerifyUser_UserNullId_ThrowsException() {
        User userWithNullId = new User();
        userWithNullId.setFirstName("Test");
        userWithNullId.setLastName("User");
        userWithNullId.setEmail("test@example.com");
        userWithNullId.setBSN(123);
        userWithNullId.setId(null); // Simulate user object from DB having null ID somehow (unlikely for real JPA)
        userWithNullId.setActive(true); // Fulfill other checks
        userWithNullId.setVerified(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithNullId)); // Mock finding a user but with null
                                                                                   // ID

        // This test case now explicitly calls the service method and asserts the
        // exception
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userService.verifyUser(1L));
        assertEquals("User ID mismatch or user is null", thrown.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("testVerifyUser - Missing email should throw IllegalArgumentException")
    void testVerifyUser_MissingEmail_ThrowsException() {
        user1.setEmail(null);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        user1.setEmail(""); // empty string
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
    }

    @Test
    @DisplayName("testVerifyUser - Missing first name should throw IllegalArgumentException")
    void testVerifyUser_MissingFirstName_ThrowsException() {
        user1.setFirstName(null);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        verify(userRepository, times(1)).findById(user1.getId());

        user1.setFirstName(""); // Reset for next assertion
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        verify(userRepository, times(2)).findById(user1.getId());
    }

    @Test
    @DisplayName("testVerifyUser - Missing last name should throw IllegalArgumentException")
    void testVerifyUser_MissingLastName_ThrowsException() {
        user1.setLastName(null);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        verify(userRepository, times(1)).findById(user1.getId());

        user1.setLastName(""); // Reset for next assertion
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        verify(userRepository, times(2)).findById(user1.getId());
    }

    @Test
    @DisplayName("testVerifyUser - Invalid BSN should throw IllegalArgumentException")
    void testVerifyUser_InvalidBsn_ThrowsException() {
        user1.setBSN(0);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        user1.setBSN(-1);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
    }

    @Test
    void testCreateUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Alice");
        userDTO.setLastName("Smith");
        userDTO.setEmail("alice@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Alice Smith");
        userDTO.setPassword("password");

        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserDTO created = userService.createUser(userDTO);

        assertNotNull(created);
        assertEquals("Alice", created.getFirstName());
        assertEquals("Smith", created.getLastName());
        assertEquals("alice@example.com", created.getEmail());
        assertEquals(10L, created.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateEmail_Throws() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("existing@example.com");
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(new User()));
        assertThrows(RuntimeException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_DuplicateUsername_Throws() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("unique@example.com");
        userDTO.setUsername("existinguser");
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(new User()));
        assertThrows(RuntimeException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testUpdateUser_Success() {
        User existing = new User();
        existing.setId(1L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setEmail("old@example.com");
        existing.setPhoneNumber("000");
        existing.setDailyLimit(BigDecimal.valueOf(100));
        existing.setBSN(111);
        existing.setVerified(false);
        existing.setActive(true);
        existing.setUsername("olduser");

        User update = new User();
        update.setFirstName("New");
        update.setLastName("Name");
        update.setEmail("new@example.com");
        update.setPhoneNumber("111");
        update.setDailyLimit(BigDecimal.valueOf(200));
        update.setBSN(222);
        update.setVerified(true);
        update.setActive(false);
        update.setUsername("newuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updated = userService.updateUser(1L, update);

        assertNotNull(updated);
        assertEquals("New", updated.getFirstName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(222, updated.getBSN());
        assertEquals("newuser", updated.getUsername());
    }

    @Test
    void testDeactivateUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO deactivated = userService.deactivateUser(1L);

        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());
    }

    @Test
    void testGetUserById_Success() {
        User user = new User();
        user.setId(5L);
        user.setFirstName("Test");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        UserDTO dto = userService.getUserById(5L);
        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("Test", dto.getFirstName());
    }

    @Test
    void testGetUserById_NotFound_Throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.getUserById(99L));
    }

    @Test
    void testToDTO_NullInput_ReturnsNull() {
        // If toDTO(List<User>) returns an empty list for null input
        assertTrue(userService.toDTO((List<User>) null).isEmpty());
    }

    @Test
    void testToDTO_SingleUser_Null_ReturnsNull() {
        assertNull(userService.toDTO((User) null));
    }

    @Test
    void testToDTO_EmptyList_ReturnsEmptyList() {
        List<UserDTO> dtos = userService.toDTO(List.of());
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void testToDTO_ListWithNullUser_SkipsNull() {
        List<User> users = Arrays.asList(user1, null, user2);
        List<UserDTO> dtos = userService.toDTO(users);
        assertNotNull(dtos);
        assertEquals(3, users.size());
        assertEquals(2, dtos.size());
        assertEquals(user1.getId(), dtos.get(0).getId());
        assertEquals(user2.getId(), dtos.get(1).getId());
    }

    @Test
    void testCreateUser_NullPassword_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword(null);

        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testDeactivateUser_NotFound_ThrowsException() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.deactivateUser(123L));
    }

    @Test
    void testUpdateUser_NotFound_ThrowsException() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());
        User update = new User();
        assertThrows(Exception.class, () -> userService.updateUser(123L, update));
    }

    @Test
    void testVerifyUser_IdMismatch_ThrowsException() {
        user1.setId(1L);
        // Mock findById(2L) to return user1 (whose id is 1L)
        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(2L));
    }

    @Test
    void testGetAllUsers_ReturnsList() {
        // Mock an empty Page<User>
        Page<User> emptyPage = Page.empty();
        when(userRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class))).thenReturn(emptyPage);
        List<UserDTO> users = userService.getAllUsers(null, null);
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testCreateUser_MissingFirstName_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(null);
        userDTO.setLastName("User");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_MissingLastName_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName(null);
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_MissingEmail_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setEmail(null);
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_MissingBSN_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(0);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_InvalidBSN_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(-5);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testVerifyUser_NullId_ThrowsException() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.verifyUser(null));
    }

    @Test
    void testGetAllUsers_RepositoryReturnsNull_ReturnsEmptyList() {

        Page<User> emptyPage = Page.empty();
        when(userRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class))).thenReturn(emptyPage);
        List<UserDTO> users = userService.getAllUsers(null, null);
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testToDTO_ListAllNulls_ReturnsEmptyList() {
        List<User> users = Arrays.asList(null, null, null);
        List<UserDTO> dtos = userService.toDTO(users);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }

    @Test
    void testCreateUser_EmptyFirstName_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("");
        userDTO.setLastName("User");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_EmptyLastName_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_EmptyEmail_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setEmail("");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_ZeroBSN_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(0);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testCreateUser_NegativeBSN_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setEmail("test@example.com");
        userDTO.setBSN(-10);
        userDTO.setPhoneNumber("1234567890");
        userDTO.setUsername("Test User");
        userDTO.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testUpdateUser_UpdatesAllFields() {
        User existing = new User();
        existing.setId(1L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setEmail("old@example.com");
        existing.setPhoneNumber("000");
        existing.setDailyLimit(BigDecimal.valueOf(100));
        existing.setBSN(111);
        existing.setVerified(false);
        existing.setActive(true);
        existing.setUsername("olduser");

        User update = new User();
        update.setFirstName("NewFirst");
        update.setLastName("NewLast");
        update.setEmail("new@example.com");
        update.setPhoneNumber("999");
        update.setDailyLimit(BigDecimal.valueOf(500));
        update.setBSN(222);
        update.setVerified(true);
        update.setActive(false);
        update.setUsername("newuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updated = userService.updateUser(1L, update);

        assertNotNull(updated);
        assertEquals("NewFirst", updated.getFirstName());
        assertEquals("NewLast", updated.getLastName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("999", updated.getPhoneNumber());
        assertEquals(BigDecimal.valueOf(500), updated.getDailyLimit());
        assertEquals(222, updated.getBSN());
        assertTrue(updated.isVerified());
        assertFalse(updated.isActive());
        assertEquals("newuser", updated.getUsername());
    }

    @Test
    void testDeactivateUser_AlreadyInactive() {
        User user = new User();
        user.setId(1L);
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO deactivated = userService.deactivateUser(1L);

        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());
    }

    @Test
    void testToDTO_UserWithNullFields() {
        User user = new User();
        user.setId(42L);
        // All other fields left null/default
        UserDTO dto = userService.toDTO(user);
        assertNotNull(dto);
        assertEquals(42L, dto.getId());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
        assertNull(dto.getEmail());
        assertNull(dto.getPhoneNumber());
        assertNull(dto.getUsername());
        assertNull(dto.getPassword());
        assertFalse(dto.isActive());
        assertFalse(dto.isVerified());
        assertNull(dto.getDailyLimit());
        assertNull(dto.getCreatedAt());
    }

    @Test
    void testCreateUser_AllFieldsSet_CorrectlyMapped() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Bob");
        userDTO.setLastName("Builder");
        userDTO.setEmail("bob@builder.com");
        userDTO.setBSN(111222333);
        userDTO.setPhoneNumber("0612345678");
        userDTO.setUsername("bobthebuilder");
        userDTO.setPassword("canwefixit");

        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });

        UserDTO created = userService.createUser(userDTO);

        assertNotNull(created);
        assertEquals("Bob", created.getFirstName());
        assertEquals("Builder", created.getLastName());
        assertEquals("bob@builder.com", created.getEmail());
        assertEquals(111222333, created.getBSN());
        assertEquals("0612345678", created.getPhoneNumber());
        assertEquals("bobthebuilder", created.getUsername());
        assertEquals(99L, created.getId());
    }

    @Test
    void testDeactivateUser_NonExistingUser_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.deactivateUser(999L));
    }

    @Test
    void testUpdateUser_NullDetails_ThrowsException() {
        User existing = new User();
        existing.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(NullPointerException.class, () -> userService.updateUser(1L, null));
    }

    @Test
    void testToDTO_ListWithMixedNullsAndUsers() {
        User userA = new User();
        userA.setId(1L);
        userA.setFirstName("A");
        User userB = new User();
        userB.setId(2L);
        userB.setFirstName("B");
        List<User> users = Arrays.asList(userA, null, userB, null);
        List<UserDTO> dtos = userService.toDTO(users);
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("A", dtos.get(0).getFirstName());
        assertEquals("B", dtos.get(1).getFirstName());
    }

    @Test
    void testGetUserById_NullId_ThrowsException() {
        assertThrows(Exception.class, () -> userService.getUserById(null));
    }

    @Test
    void testVerifyUser_AlreadyVerified() {
        user1.setVerified(true);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDTO dto = (UserDTO) userService.verifyUser(user1.getId());
        assertTrue(dto.isVerified());
    }

    @Test
    void testCreateUser_UsernameNull_AllowsCreation() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("No");
        userDTO.setLastName("Username");
        userDTO.setEmail("nouser@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPhoneNumber("0612345678");
        userDTO.setUsername(null);
        userDTO.setPassword("pw");
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(123L);
            return u;
        });
        UserDTO created = userService.createUser(userDTO);
        assertNotNull(created);
        assertEquals("No", created.getFirstName());
        assertEquals("Username", created.getLastName());
        assertEquals("nouser@example.com", created.getEmail());
        assertEquals(123L, created.getId());
    }

    @Test
    void testCreateUser_UsernameExistsButNull_Allowed() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("No");
        userDTO.setLastName("Username");
        userDTO.setEmail("nouser2@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPassword("pw");
        userDTO.setUsername(null);
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(124L);
            return u;
        });
        UserDTO created = userService.createUser(userDTO);
        assertNotNull(created);
        assertEquals("No", created.getFirstName());
        assertEquals("Username", created.getLastName());
        assertEquals("nouser2@example.com", created.getEmail());
        assertEquals(124L, created.getId());
    }

    @Test
    void testCreateUser_UsernameExists_Throws() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Dup");
        userDTO.setLastName("User");
        userDTO.setEmail("dup@example.com");
        userDTO.setBSN(123456789);
        userDTO.setPassword("pw");
        userDTO.setUsername("dupuser");
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(new User()));
        assertThrows(RuntimeException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void testUpdateUser_ThrowsIfNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.updateUser(404L, new User()));
    }

    @Test
    void testDeactivateUser_ThrowsIfNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.deactivateUser(404L));
    }

    @Test
    void testGetUserById_ThrowsIfNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.getUserById(404L));
    }

    @Test
    void testVerifyUser_ThrowsIfNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.verifyUser(404L));
    }

    @Test
    void testVerifyUser_ThrowsIfIdMismatch() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(2L));
    }

    @Test
    void testMakeUserFilterSpecification_NullParams()
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // Should not throw and should return null
        assertNull(userService.getClass()
                .getDeclaredMethod("makeUserFilterSpecification", Map.class)
                .invoke(userService, (Object) null));
    }

    @Test
    void testMakeUserFilterSpecification_EmptyParams() throws Exception {
        // Should not throw and should return null
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Object result = method.invoke(userService, Map.of());
        assertNull(result);
    }

    @Test
    void testMakeUserFilterSpecification_EmailFilter() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("email", "test@example.com");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_FirstNameFilter() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("firstName", "John");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_LastNameFilter() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("lastName", "Doe");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_ActiveFilter() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("active", "true");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_VerifiedFilter() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("verified", "false");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_CreatedBeforeFilter() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        String date = LocalDateTime.now().toString();
        Map<String, String> params = Map.of("createdBefore", date);
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_CreatedAfterFilter() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        String date = LocalDateTime.now().toString();
        Map<String, String> params = Map.of("createdAfter", date);
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_MultipleFilters() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        String date = LocalDateTime.now().toString();
        Map<String, String> params = Map.of(
                "email", "test@example.com",
                "firstName", "John",
                "lastName", "Doe",
                "active", "true",
                "verified", "false",
                "createdBefore", date,
                "createdAfter", date);
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_UserNameKeyIgnored() throws Exception {
        // The code checks for "userName" but uses "firstName" for the value
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("userName", "ignored", "firstName", "John");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_HandlesInvalidDate() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        // Invalid date string should throw DateTimeParseException inside the lambda if
        // executed
        Map<String, String> params = Map.of("createdBefore", "not-a-date");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_HandlesBooleanStrings() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("active", "TRUE", "verified", "FALSE");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_HandlesCaseInsensitiveKeys() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        // The method expects lower-case keys, so this should not add any predicates
        Map<String, String> params = Map.of("EMAIL", "test@example.com");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    @Test
    void testMakeUserFilterSpecification_HandlesExtraUnknownKeys() throws Exception {
        var method = userService.getClass().getDeclaredMethod("makeUserFilterSpecification", Map.class);
        method.setAccessible(true);
        Map<String, String> params = Map.of("unknown", "value", "email", "test@example.com");
        Object spec = method.invoke(userService, params);
        assertNotNull(spec);
    }

    // --- toDTO conversions ---
    @Test
    @DisplayName("toDTO - Should correctly convert User to UserDTO")
    void toDTO_ShouldConvertUserToUserDTO() {
        UserDTO dto = userService.toDTO(user1);

        assertNotNull(dto);
        assertEquals(user1.getId(), dto.getId());
        assertEquals(user1.getFirstName(), dto.getFirstName());
        assertEquals(user1.getLastName(), dto.getLastName());
        assertEquals(user1.getEmail(), dto.getEmail());
        assertEquals(user1.getPhoneNumber(), dto.getPhoneNumber());
        assertEquals(user1.getBSN(), dto.getBSN());
        assertEquals(user1.getUsername(), dto.getUsername());
        assertEquals(user1.getPassword(), dto.getPassword()); // As per your current toDTO implementation
        assertEquals(user1.isActive(), dto.isActive());
        assertEquals(user1.isVerified(), dto.isVerified());
        assertEquals(user1.getDailyLimit(), dto.getDailyLimit());
        assertEquals(user1.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    @DisplayName("toDTO - Should return null if input User is null")
    void toDTO_NullUser_ReturnsNull() {
        UserDTO dto = userService.toDTO((User) null);
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO (List) - Should correctly convert List<User> to List<UserDTO>")
    void toDTOList_ShouldConvertListUserToListUserDTO() {
        List<User> users = Arrays.asList(user1, user2);
        List<UserDTO> dtos = userService.toDTO(users);

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals(user1.getId(), dtos.get(0).getId());
        assertEquals(user2.getId(), dtos.get(1).getId());
    }

    @Test
    @DisplayName("toDTO (List) - Should return empty list if input List<User> is empty")
    void toDTOList_EmptyList_ReturnsEmptyList() {
        List<UserDTO> dtos = userService.toDTO(Collections.emptyList());
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}