package com.krachbank.api.controllers;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with the controller under test
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }


    @Test
    @DisplayName("POST /users/{id}/verify - Should verify a user and return UserDTO")
    void verifyUser_ShouldReturnVerifiedUser() throws Exception {
        UserDTO verifiedUser = new UserDTO();
        verifiedUser.setId(1L);
        verifiedUser.setVerified(true);

        when(userService.verifyUser(1L)).thenReturn(verifiedUser);

        mockMvc.perform(post("/users/{id}/verify", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("POST /users/{id}/verify - Should return 400 if verification fails")
    void verifyUser_ShouldReturnBadRequestOnIllegalArgument() throws Exception {
        when(userService.verifyUser(2L)).thenThrow(new IllegalArgumentException("Verification failed"));

        mockMvc.perform(post("/users/{id}/verify", 2L).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Verification failed"));
    }
}
