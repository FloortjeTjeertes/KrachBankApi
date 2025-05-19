package com.krachbank.api.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.DTO;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;
import com.krachbank.api.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getUsers() {
        return userService.getUsers();
    }

    @PostMapping("{id}/verify")
    public UserDTO verifyUser(User user) {
        try {
            return (UserDTO) userService.verifyUser(user);
        } catch (IllegalArgumentException e) {
            // Handle the exception as needed, e.g., log it or return an error response
            System.out.println("Error creating user: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
}
