package com.krachbank.api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;
import com.krachbank.api.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController implements Controller<User, UserDTO> {
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

    @Override
    public User toModel(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setDailyLimit(dto.getTransferLimit());
        user.setCreatedAt(dto.getCreatedAt());
        user.setVerified(dto.isVerified());
        user.setActive(dto.isActive());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setBsn(dto.getBSN());
        return user;
    }
}
