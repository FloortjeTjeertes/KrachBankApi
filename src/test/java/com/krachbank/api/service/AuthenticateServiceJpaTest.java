package com.krachbank.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.krachbank.api.dto.AuthenticationResultDTO;
import com.krachbank.api.dto.LoginRequest;
import com.krachbank.api.dto.RegisterRequest;
import com.krachbank.api.exceptions.InvalidCredentialsException;
import com.krachbank.api.exceptions.UserAlreadyExistsException;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.AuthenticationRepository;

class AuthenticationServiceJpaTest {

    private AuthenticationServiceJpa authenticationService;

    // Mocked Dependencies
    private AuthenticationRepository authenticationRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        authenticationRepository = mock(AuthenticationRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);
        emailService = mock(EmailService.class);

        // Inject mocks into the service
        authenticationService = new AuthenticationServiceJpa(
                authenticationRepository,
                passwordEncoder,
                jwtService,
                authenticationManager
                , emailService
        );
    }

    // --- Register Tests ---

    @Test
    @DisplayName("Register - Should register a new user successfully and return token")
    void register_Success() throws UserAlreadyExistsException {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "testuser", "password123", "John", "Doe", "john.doe@example.com", "1234567890", "123456789"
        );

        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("john.doe@example.com");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setPhoneNumber("1234567890");
        newUser.setBSN("123456789");
        newUser.setVerified(false);
        newUser.setActive(true);
        newUser.setDailyLimit(BigDecimal.valueOf(0.0));
        newUser.setPassword("encodedPassword"); 

        // Mock repository behavior
        when(authenticationRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(authenticationRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(authenticationRepository.save(any(User.class))).thenReturn(newUser); // Return the mock user after saving
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mockJwtToken");

        // Act
        AuthenticationResultDTO result = authenticationService.register(request);

        // Assert
        assertNotNull(result);
        assertThat(result.getToken()).isEqualTo("mockJwtToken");
        assertThat(result.getUserDetails().getUsername()).isEqualTo("testuser");
        assertThat(result.getUserDetails().getEmail()).isEqualTo("john.doe@example.com");

        // Verify interactions
        verify(authenticationRepository, times(1)).findByUsername(request.getUsername());
        verify(authenticationRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(authenticationRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Register - Should throw UserAlreadyExistsException if username exists")
    void register_UsernameExists_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "existinguser", "password123", "John", "Doe", "john.doe@example.com", "1234567890", "123456789"
        );
        when(authenticationRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        // Act & Assert
        UserAlreadyExistsException thrown = assertThrows(UserAlreadyExistsException.class, () -> {
            authenticationService.register(request);
        });

        assertThat(thrown.getMessage()).contains("username 'existinguser' already exists");
        verify(authenticationRepository, times(1)).findByUsername(request.getUsername());
        verify(authenticationRepository, never()).findByEmail(anyString()); // Email check should not be reached
        verify(passwordEncoder, never()).encode(anyString());
        verify(authenticationRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Register - Should throw UserAlreadyExistsException if email exists")
    void register_EmailExists_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "testuser", "password123", "John", "Doe", "existing@example.com", "1234567890", "123456789"
        );
        when(authenticationRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(authenticationRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        UserAlreadyExistsException thrown = assertThrows(UserAlreadyExistsException.class, () -> {
            authenticationService.register(request);
        });

        assertThat(thrown.getMessage()).contains("email 'existing@example.com' already exists");
        verify(authenticationRepository, times(1)).findByUsername(request.getUsername());
        verify(authenticationRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(authenticationRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    // --- Login Tests ---

    @Test
    @DisplayName("Login - Should authenticate user successfully and return token")
    void login_Success() throws InvalidCredentialsException {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");
        User foundUser = new User();
        foundUser.setId(1L);
        foundUser.setUsername("testuser");
        foundUser.setEmail("john.doe@example.com");
        foundUser.setFirstName("John");
        foundUser.setLastName("Doe");

        // Mock AuthenticationManager behavior
        // When authenticate is called with the correct token, it should not throw
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class)); // Return a dummy Authentication object

        // Mock repository behavior after successful authentication
        when(authenticationRepository.findByUsername(request.getEmail())).thenReturn(Optional.of(foundUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mockJwtToken");

        // Act
        AuthenticationResultDTO result = authenticationService.login(request);

        // Assert
        assertNotNull(result);
        assertThat(result.getToken()).isEqualTo("mockJwtToken");
        assertThat(result.getUserDetails().getUsername()).isEqualTo("testuser");

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        verify(authenticationRepository, times(1)).findByUsername(request.getEmail());
        verify(jwtService, times(1)).generateToken(foundUser); // Directly verify with 'foundUser'
    }

    @Test
    @DisplayName("Login - Should throw InvalidCredentialsException for bad credentials")
    void login_BadCredentials_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("wronguser", "wrongpass");

        // Mock AuthenticationManager to throw BadCredentialsException
        doThrow(new BadCredentialsException("Bad credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        InvalidCredentialsException thrown = assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.login(request);
        });

        assertThat(thrown.getMessage()).isEqualTo("Invalid username or password.");
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        verify(authenticationRepository, never()).findByUsername(anyString()); // Should not attempt to find user
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Login - Should throw InvalidCredentialsException if user not found after auth (unlikely)")
    void login_UserNotFoundAfterAuth_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");

        // Mock AuthenticationManager to succeed
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Mock repository to *not* find the user after successful authentication
        when(authenticationRepository.findByUsername(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException thrown = assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.login(request);
        });

        assertThat(thrown.getMessage()).isEqualTo("User not found after successful authentication (should not happen).");
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        verify(authenticationRepository, times(1)).findByUsername(request.getEmail());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    // --- findByUsername Test ---
    @Test
    @DisplayName("findByUsername - Should return user if found")
    void findByUsername_UserFound() {
        User user = new User();
        user.setUsername("existinguser");
        when(authenticationRepository.findByUsername("existinguser")).thenReturn(Optional.of(user));

        Optional<User> foundUser = authenticationService.findByUsername("existinguser");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("existinguser");
        verify(authenticationRepository, times(1)).findByUsername("existinguser");
    }

    @Test
    @DisplayName("findByUsername - Should return empty optional if user not found")
    void findByUsername_UserNotFound() {
        when(authenticationRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> foundUser = authenticationService.findByUsername("nonexistent");

        assertThat(foundUser).isNotPresent();
        verify(authenticationRepository, times(1)).findByUsername("nonexistent");
    }
}