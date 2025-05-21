package com.krachbank.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

public class UserServiceJpaTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceJPA userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(Long.valueOf(1L));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setActive(true);
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setDailyLimit("1000");
        user.setPhoneNumber("123456789");
        user.setBsn(123456789);

        userDTO = new UserDTO();
        userDTO.setId(Long.valueOf(1L));
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setIsActive(true);
        userDTO.setIsVerified(false);
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setTransferLimit("1000");
        userDTO.setPhoneNumber("123456789");
        userDTO.setBSN(123456789);
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(Long.valueOf(1L));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");

        verify(userRepository).findById(Long.valueOf(1L));
        verify(modelMapper).map(user, UserDTO.class);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(Long.valueOf(2L))).thenReturn(Optional.empty());

        // expect your service to throw a RuntimeException or custom NotFoundException
        assertThatThrownBy(() -> userService.getUserById(Long.valueOf(2L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testCreateUser() {
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO created = userService.createUser(userDTO);

        assertThat(created).isNotNull();
        assertThat(created.getIsActive()).isTrue();
        assertThat(created.getIsVerified()).isFalse();

        verify(userRepository).save(user);
    }

    @Test
    void testDeactivateUser() {
        when(userRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDTO deactivated = userService.deactivateUser(Long.valueOf(1L));

        assertThat(deactivated).isNotNull();
        verify(userRepository).findById(Long.valueOf(1L));
        verify(userRepository).save(user);
        assertThat(user.isActive()).isFalse();
    }
    @Test
    void testVerifyUser() {
        when(userRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDTO verified = userService.verifyUser(Long.valueOf(1L));

        assertThat(verified).isNotNull();
        verify(userRepository).findById(Long.valueOf(1L));
        verify(userRepository).save(user);
        assertThat(user.isVerified()).isTrue();
    }
    @Test
    void testUpdateUser() {
        when(userRepository.findById(Long.valueOf(1L))).thenReturn(Optional.of(user));
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO updated = userService.updateUser(Long.valueOf(1L), userDTO);

        assertThat(updated).isNotNull();
        verify(userRepository).findById(Long.valueOf(1L));
        verify(userRepository).save(user);
    }
    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        List<UserDTO> result = userService.getAllUsers(new HashMap<>());

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");

        verify(userRepository).findAll();
        verify(modelMapper).map(user, UserDTO.class);
    }
    @Test
    void testGetAllUsersWithFilters() {
        Map<String, String> filters = new HashMap<>();
        filters.put("isActive", "true");
        filters.put("isVerified", "false");

        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        List<UserDTO> result = userService.getAllUsers(filters);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");

        verify(userRepository).findAll();
        verify(modelMapper).map(user, UserDTO.class);
    }
}

