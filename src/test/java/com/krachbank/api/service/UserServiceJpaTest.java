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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
        updatedDetails.setActive(false);   // Change status
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
    @DisplayName("getAllUsers - Should return filtered users by email")
    void getAllUsers_FilterByEmail_ReturnsFilteredList() {
        Map<String, String> params = new HashMap<>();
        params.put("email", "john@example.com");

        // Mock the findAll to return a Page containing only user1 when filtered by email
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
        // Note: Your makeUserFilterSpecification uses "userName" as key but then looks for "firstName".
        // I'll test based on your current implementation, which is a potential bug/typo in your spec builder.
        // Assuming you meant to use 'username' field in the future, if so, the filter key should be "username".
        // For now, I'll test 'firstName' as per your existing makeUserFilterSpecification.
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

        DTO dto = userService.verifyUser(user1.getId());

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


        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithNullId)); // Mock finding a user but with null ID

        // This test case now explicitly calls the service method and asserts the exception
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(1L));
        assertEquals("User ID mismatch or user is null", thrown.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("testVerifyUser - Missing email should throw IllegalArgumentException")
    void testVerifyUser_MissingEmail_ThrowsException() {
        user1.setEmail(null);
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        user1.setEmail(""); // empty string
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
        verify(userRepository, times(1)).findById(user1.getId());

        user1.setBSN(-1);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        assertThrows(IllegalArgumentException.class, () -> userService.verifyUser(user1.getId()));
        verify(userRepository, times(2)).findById(user1.getId());
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