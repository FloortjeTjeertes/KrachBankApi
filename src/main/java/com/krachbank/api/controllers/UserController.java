package com.krachbank.api.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController implements Controller<User, UserDTO> {
    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @PostMapping("/{id}/verify")
    public UserDTO verifyUser(@PathVariable Long id) {
        try {
            return (UserDTO) userService.verifyUser(id);
        } catch (IllegalArgumentException e) {
            // Handle the exception and return an appropriate response
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}/accounts")
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

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDTO> deactivateUser(@PathVariable Long id) {
        try {
            UserDTO deactivatedUser = userService.deactivateUser(id);
            return ResponseEntity.ok(deactivatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Optional: Get all users with filter params (if needed)
    @GetMapping()
    public List<UserDTO> getAllUsers(@RequestParam(required = false) Map<String, String> params) {
        UserFilter filter = new UserFilter();
        return userService.getAllUsers(params, filter);
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
