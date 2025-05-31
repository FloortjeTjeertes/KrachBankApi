package com.krachbank.api.controllers;

import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.dto.UserDTOResponse;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.krachbank.api.models.Account;
import com.krachbank.api.models.User;

@RestController
@RequestMapping("/users")
public class UserController implements Controller<User, UserDTO> {
    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getUsers() {
        return userService.getUsers();
    }

    @PostMapping("/{id}/verify")
    public UserDTO verifyUser( @PathVariable Long id) {
      try {
            return (UserDTO) userService.verifyUser( id);
        } catch (IllegalArgumentException e) {
            // Handle the exception and return an appropriate response
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ✅ Add this for signup
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
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
    public User toModel(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setDailyLimit(dto.getDailyLimit());
        user.setCreatedAt(dto.getCreatedAt());
        user.setVerified(dto.isVerified());
        user.setActive(dto.isActive());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setBSN(dto.getBSN());
        return user;
    }
}
