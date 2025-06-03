/*
package com.krachbank.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krachbank.api.dto.AuthenticationResultDTO;
import com.krachbank.api.dto.LoginRequest;
import com.krachbank.api.dto.RegisterRequest;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.exceptions.InvalidCredentialsException;
import com.krachbank.api.service.AuthenticationService;
import com.krachbank.api.service.JwtService;
import com.krachbank.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; // Often useful for mock beans
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Autowire the mocked beans that are now provided via TestConfig
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;


    // Define a nested configuration class to provide mock beans
    @Configuration
    static class TestConfig {
        @Bean
        @Primary // Use @Primary to ensure this mock is preferred if other beans exist
        public AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean
        @Primary
        public JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        @Primary
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        @Primary
        public AuthenticationService authenticationService() {
            return mock(AuthenticationService.class);
        }
    }


    private RegisterRequest registerRequest;
    private UserDTO createdUserDTO;
    private LoginRequest loginRequest;
    private AuthenticationResultDTO authResultDTO;

    @BeforeEach
    void setUp() {
        // Setup for Register User
        registerRequest = new RegisterRequest(
                "testuser", "password123", "John", "Doe", "john.doe@example.com", "1234567890", 123456789
        );

        createdUserDTO = new UserDTO();
        createdUserDTO.setId(1L);
        createdUserDTO.setUsername("JohnDoe"); // Based on your controller's logic
        createdUserDTO.setEmail("john.doe@example.com");
        createdUserDTO.setFirstName("John");
        createdUserDTO.setLastName("Doe");
        createdUserDTO.setBSN(123456789);
        createdUserDTO.setPhoneNumber("1234567890");
        createdUserDTO.setCreatedAt(LocalDateTime.now());
        createdUserDTO.setVerified(false);
        createdUserDTO.setActive(true);
        createdUserDTO.setDailyLimit(BigDecimal.ZERO);

        // Setup for Authenticate User (Login)
        loginRequest = new LoginRequest("testuser", "password123");

        authResultDTO = new AuthenticationResultDTO(
                "mockJwtToken",
                new UserDTO(
                        2L, "testuser", "test@example.com", "Jane", "Smith", 987654321, "0987654321",
                        true, true, BigDecimal.valueOf(500.00), LocalDateTime.now()
                )
        );
    }

    // --- Register User Tests ---

    @Test
    @DisplayName("POST /auth/register - Should register a user successfully")
    void registerUser_Success() throws Exception {
        // Mock the UserService to return the created UserDTO
        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUserDTO);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully with email: " + createdUserDTO.getEmail()));

        verify(userService).createUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("POST /auth/register - Should return 400 Bad Request on RuntimeException")
    void registerUser_RuntimeException() throws Exception {
        String errorMessage = "User with that email already exists!";
        doThrow(new RuntimeException(errorMessage)).when(userService).createUser(any(UserDTO.class));

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    // --- Authenticate User (Login) Tests ---

    @Test
    @DisplayName("POST /auth/login - Should authenticate user successfully and return token")
    void authenticateUser_Success() throws Exception {
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResultDTO);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockJwtToken"))
                .andExpect(jsonPath("$.userDetails.username").value("testuser"))
                .andExpect(jsonPath("$.userDetails.email").value("test@example.com"));

        verify(authenticationService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 401 Unauthorized on InvalidCredentialsException")
    void authenticateUser_InvalidCredentials() throws Exception {
        doThrow(new InvalidCredentialsException("Invalid username or password.")).when(authenticationService).login(any(LoginRequest.class));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 500 Internal Server Error on generic Exception")
    void authenticateUser_GenericException() throws Exception {
        doThrow(new RuntimeException("Database error during login.")).when(authenticationService).login(any(LoginRequest.class));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(""));
    }
}*/
