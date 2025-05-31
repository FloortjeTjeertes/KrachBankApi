package com.krachbank.api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.UserDTOResponse;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController implements Controller<User, UserDTOResponse> {
    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTOResponse> getUsers() {
        return userService.getUsers();
    }

    @PostMapping("{id}/verify")
    public UserDTOResponse verifyUser(User user) {
        try {
            return (UserDTOResponse) userService.verifyUser(user);
        } catch (IllegalArgumentException e) {
            // Handle the exception as needed, e.g., log it or return an error response
            System.out.println("Error creating user: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }

    @GetMapping("{id}/accounts")
    public ResponseEntity<?> getAccountsForUser(@PathVariable Long id, UserFilter filter) {
        try {
            System.out.println("Fetching accounts for user with ID: " + id);
            List<AccountDTOResponse> accountDTOs = new ArrayList<AccountDTOResponse>();
            Page<Account> accountsPage = accountService.getAccountsByUserId(id, filter);
            List<Account> accounts = accountsPage.getContent();
            for (Account account : accounts) {
                accountDTOs.add(accountService.toDTO(account));
            }
            return ResponseEntity.ok(accountDTOs);
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }

    }

    @Override
    public User toModel(UserDTOResponse dto) {
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
