package com.krachbank.api.service;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.dto.UserDTOResponse;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    }

    @Test
    void testGetUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserDTO> users = userService.toDTO(userRepository.findAll());

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(user1.getId(), users.get(0).getId());
        assertEquals(user2.getId(), users.get(1).getId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testVerifyUser() {
        user1.setVerified(false);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setVerified(true);
            return savedUser;
        });

        UserDTO dto = (UserDTO) userService.verifyUser(user1.getId());

        assertNotNull(dto);
        assertEquals(user1.getId(), dto.getId());
        assertEquals(user1.getEmail(), dto.getEmail());
        assertTrue(dto.isVerified(), "User should be verified after verifyUser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testVerifyUser_NullUser_ThrowsException() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> userService.verifyUser(null));
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
}
