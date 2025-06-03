/*
package com.krachbank.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) // Specify the controller to test
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // Used to perform HTTP requests

    @MockBean // Mocks the UserService dependency
    private UserService userService;

    @MockBean // Mocks the AccountService dependency
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to/from JSON

    private UserDTO userDTO1;
    private UserDTO userDTO2;
    private User userModel1; // For @RequestBody conversions

    private Account account1;
    private Account account2;
    private AccountDTOResponse accountDTOResponse1;
    private AccountDTOResponse accountDTOResponse2;

    @BeforeEach
    void setUp() {
        // Setup UserDTOs for responses
        userDTO1 = new UserDTO();
        userDTO1.setId(1L);
        userDTO1.setFirstName("John");
        userDTO1.setLastName("Doe");
        userDTO1.setEmail("john.doe@example.com");
        userDTO1.setBSN(123456789);
        userDTO1.setPhoneNumber("111-222-3333");
        userDTO1.setUsername("john.doe");
        userDTO1.setActive(true);
        userDTO1.setVerified(true);
        userDTO1.setDailyLimit(BigDecimal.valueOf(1000.00));
        userDTO1.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));

        userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setFirstName("Jane");
        userDTO2.setLastName("Smith");
        userDTO2.setEmail("jane.smith@example.com");
        userDTO2.setBSN(987654321);
        userDTO2.setPhoneNumber("444-555-6666");
        userDTO2.setUsername("jane.smith");
        userDTO2.setActive(true);
        userDTO2.setVerified(false);
        userDTO2.setDailyLimit(BigDecimal.valueOf(500.00));
        userDTO2.setCreatedAt(LocalDateTime.of(2024, 2, 15, 12, 30));

        // Setup User model for request bodies (e.g., for updateUser)
        userModel1 = new User();
        userModel1.setId(1L);
        userModel1.setFirstName("Johnathon"); // Modified name
        userModel1.setLastName("Doe");
        userModel1.setEmail("john.doe@example.com");
        userModel1.setBSN(123456789);
        userModel1.setPhoneNumber("111-222-3333");
        userModel1.setUsername("john.doe");
        userModel1.setActive(true);
        userModel1.setVerified(true);
        userModel1.setDailyLimit(BigDecimal.valueOf(1000.00));
        userModel1.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));


        // Setup Account objects and DTOs for testing getAccountsForUser
        account1 = new Account();
        account1.setId(101L);
        account1.setIBAN("NLxxINGB0000000001");
        account1.setAccountType("CURRENT");
        account1.setBalance(BigDecimal.valueOf(5000));
        account1.setUserId(1L);

        account2 = new Account();
        account2.setId(102L);
        account2.setIBAN("NLxxINGB0000000002");
        account2.setAccountType("SAVINGS");
        account2.setBalance(BigDecimal.valueOf(10000));
        account2.setUserId(1L);

        accountDTOResponse1 = new AccountDTOResponse();
        accountDTOResponse1.setId(101L);
        accountDTOResponse1.setIBAN("NLxxINGB0000000001");
        accountDTOResponse1.setAccountType("CURRENT");
        accountDTOResponse1.setBalance(BigDecimal.valueOf(5000));
        accountDTOResponse1.setUserId(1L);

        accountDTOResponse2 = new AccountDTOResponse();
        accountDTOResponse2.setId(102L);
        accountDTOResponse2.setIBAN("NLxxINGB0000000002");
        accountDTOResponse2.setAccountType("SAVINGS");
        accountDTOResponse2.setBalance(BigDecimal.valueOf(10000));
        accountDTOResponse2.setUserId(1L);
    }

    @Test
    @DisplayName("GET /users/{id} - Should return user by ID")
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDTO1);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("GET /users/{id} - Should return 404 if user not found")
    void getUserById_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users - Should return all users")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        List<UserDTO> allUsers = Arrays.asList(userDTO1, userDTO2);
        // Mocking with any(Map.class) and any(UserFilter.class) to match the controller's call
        when(userService.getAllUsers(any(Map.class), any(UserFilter.class)))
                .thenReturn(allUsers);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("PUT /users/{id} - Should update an existing user")
    void updateUser_ShouldUpdateUser() throws Exception {
        // Mock the userService.updateUser call to return a UserDTO with updated info
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setFirstName("Johnathon"); // Updated name
        updatedUserDTO.setLastName("Doe");
        updatedUserDTO.setEmail("john.doe@example.com");
        updatedUserDTO.setBSN(123456789);
        updatedUserDTO.setPhoneNumber("111-222-3333");
        updatedUserDTO.setUsername("john.doe");
        updatedUserDTO.setActive(true);
        updatedUserDTO.setVerified(true);
        updatedUserDTO.setDailyLimit(BigDecimal.valueOf(1000.00));
        updatedUserDTO.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUserDTO);

        mockMvc.perform(put("/users/{id}", 1L)
                        .with(csrf()) // Required for PUT/POST/DELETE with Spring Security
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userModel1))) // Use userModel1 for request body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Johnathon")); // Assert the updated field
    }

    @Test
    @DisplayName("PUT /users/{id} - Should return 404 if user to update not found")
    void updateUser_ShouldReturnNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(userService).updateUser(eq(99L), any(User.class));

        mockMvc.perform(put("/users/{id}", 99L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userModel1)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /users/{id} - Should deactivate a user")
    void deactivateUser_ShouldDeactivateUser() throws Exception {
        UserDTO deactivatedUserDTO = new UserDTO();
        deactivatedUserDTO.setId(1L);
        deactivatedUserDTO.setActive(false); // Simulate deactivation
        deactivatedUserDTO.setFirstName("John");
        deactivatedUserDTO.setLastName("Doe");

        when(userService.deactivateUser(1L)).thenReturn(deactivatedUserDTO);

        mockMvc.perform(delete("/users/{id}", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(false)); // Assert deactivated
    }

    @Test
    @DisplayName("DELETE /users/{id} - Should return 404 if user to deactivate not found")
    void deactivateUser_ShouldReturnNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(userService).deactivateUser(anyLong());

        mockMvc.perform(delete("/users/{id}", 99L).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users/{id}/verify - Should verify a user")
    void verifyUser_ShouldVerifyUser() throws Exception {
        UserDTO verifiedUserDTO = new UserDTO();
        verifiedUserDTO.setId(1L);
        verifiedUserDTO.setVerified(true); // Simulate verification
        verifiedUserDTO.setFirstName("John");
        verifiedUserDTO.setLastName("Doe");

        when(userService.verifyUser(1L)).thenReturn(verifiedUserDTO);

        mockMvc.perform(post("/users/{id}/verify", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.verified").value(true)); // Assert verified
    }

    @Test
    @DisplayName("POST /users/{id}/verify - Should return 400 for invalid verification request")
    void verifyUser_ShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Email is required")).when(userService).verifyUser(anyLong());

        mockMvc.perform(post("/users/{id}/verify", 1L).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is required")); // Assert error message
    }

    @Test
    @DisplayName("GET /users/{id}/accounts - Should return accounts for a user")
    void getAccountsForUser_ShouldReturnAccounts() throws Exception {
        List<Account> accountsList = Arrays.asList(account1, account2);
        Page<Account> accountsPage = new PageImpl<>(accountsList);

        when(accountService.getAccountsByUserId(eq(1L), any(UserFilter.class))).thenReturn(accountsPage);
        when(accountService.toDTO(account1)).thenReturn(accountDTOResponse1);
        when(accountService.toDTO(account2)).thenReturn(accountDTOResponse2);

        mockMvc.perform(get("/users/{id}/accounts", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(101L))
                .andExpect(jsonPath("$[0].iban").value("NLxxINGB0000000001"))
                .andExpect(jsonPath("$[1].id").value(102L))
                .andExpect(jsonPath("$[1].iban").value("NLxxINGB0000000002"));
    }

    @Test
    @DisplayName("GET /users/{id}/accounts - Should return 500 if an error occurs")
    void getAccountsForUser_ShouldReturnInternalServerError() throws Exception {
        // Simulate a generic exception from the service layer
        doThrow(new RuntimeException("Database connection failed")).when(accountService)
                .getAccountsByUserId(anyLong(), any(UserFilter.class));

        mockMvc.perform(get("/users/{id}/accounts", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database connection failed"))
                .andExpect(jsonPath("$.code").value(500));
    }
}*/
