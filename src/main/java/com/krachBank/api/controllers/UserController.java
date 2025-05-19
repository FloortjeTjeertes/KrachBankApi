package com.krachbank.api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String createdBefore,
            @RequestParam(required = false) String createdAfter,
            @RequestParam(required = false) String isVerified,
            @RequestParam(required = false) String isActive,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    // add more parameters as needed
    ) {
        List<UserDTO> users = userService.getUsers();
        // Filter by email if provided
        if (email != null) {
            users = users.stream()
                    .filter(u -> email.equals(u.getEmail()))
                    .toList();
        }
        // Filter by createdBefore if provided
        if (createdBefore != null) {
            users = users.stream()
                    .filter(u -> {
                        // Assuming getCreated() returns a String or a Date
                        // Adjust parsing as needed
                        return u.getCreatedAt() != null && u.getCreatedAt().compareTo(createdBefore) < 0;
                    })
                    .toList();
        }
        // Filter by createdAfter if provided
        if (createdAfter != null) {
            users = users.stream()
                    .filter(u -> {
                        return u.getCreatedAt() != null && u.getCreatedAt().compareTo(createdAfter) > 0;
                    })
                    .toList();
        }
        // Filter by isVerified if provided
        if (isVerified != null) {
            users = users.stream()
                    .filter(u -> isVerified.equalsIgnoreCase(String.valueOf(u.isVerified())))
                    .toList();
        }
        // Filter by isActive if provided
        if (isActive != null) {
            users = users.stream()
                    .filter(u -> isActive.equalsIgnoreCase(String.valueOf(u.isActive())))
                    .toList();
        }
        // Filter by lastName if provided
        if (lastName != null) {
            users = users.stream()
                    .filter(u -> lastName.equalsIgnoreCase(u.getLastName()))
                    .toList();
        }
        // Apply offset and limit
        return users.stream()
                .skip(offset != null && offset > 0 ? offset : 0)
                .limit(limit != null && limit > 0 ? limit : 100)
                .toList();
    }

}
