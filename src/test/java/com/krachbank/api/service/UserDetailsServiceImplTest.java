package com.krachbank.api.service;

import com.krachbank.api.models.User;
import com.krachbank.api.repository.AuthenticationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    private UserDetailsServiceImpl userDetailsService;

    // Mocked Dependency
    private AuthenticationRepository authenticationRepository;

    @BeforeEach
    void setUp() {
        // Initialize the mock repository
        authenticationRepository = mock(AuthenticationRepository.class);

        // Instantiate the service with the mocked dependency
        userDetailsService = new UserDetailsServiceImpl(authenticationRepository);
    }

    @Test
    @DisplayName("loadUserByUsername - Should return UserDetails when user exists")
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        // Arrange
        String username = "testuser";
        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword("encodedPassword"); // Password is required for UserDetails
        // Set other necessary UserDetails properties for a minimal valid user
        mockUser.setId(1L);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setActive(true);
        mockUser.setVerified(true);


        when(authenticationRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue(); // Assuming 'isActive' maps to enabled
        assertThat(userDetails.isAccountNonLocked()).isTrue(); // Assuming accounts are not locked by default
        assertThat(userDetails.isCredentialsNonExpired()).isTrue(); // Assuming credentials don't expire by default
        assertThat(userDetails.isAccountNonExpired()).isTrue(); // Assuming accounts don't expire by default

        // Verify that the repository method was called
        verify(authenticationRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("loadUserByUsername - Should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_UserDoesNotExist_ThrowsException() {
        // Arrange
        String username = "nonexistentuser";
        when(authenticationRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException thrown = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });

        assertThat(thrown.getMessage()).isEqualTo("User not found with username: " + username);

        // Verify that the repository method was called
        verify(authenticationRepository, times(1)).findByUsername(username);
    }
}