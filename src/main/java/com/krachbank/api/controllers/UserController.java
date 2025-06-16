package com.krachbank.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.krachbank.api.dto.PaginatedResponseDTO;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.filters.UserFilter;
import com.krachbank.api.mappers.AccountMapper;
import com.krachbank.api.mappers.TransactionMapper;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.TransactionService;
import com.krachbank.api.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionsService;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    public UserController(UserService userService, AccountService accountService,
            TransactionService transactionService, AccountMapper accountMapper, TransactionMapper transactionMapper) {
        this.transactionsService = transactionService;
        this.accountService = accountService;
        this.userService = userService;
        this.accountMapper = accountMapper;
        this.transactionMapper =  transactionMapper;

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
    public ResponseEntity<?> getAccountsForUser(@PathVariable Long id, @ModelAttribute AccountFilter filter) {

        try {

            if (id == null) {
                ErrorDTOResponse error = new ErrorDTOResponse("User ID cannot be null", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            if (filter == null) {
                filter = new AccountFilter();
            }

            Page<Account> accountsPage = accountService.getAccountsByUserId(id, filter);
            int size = accountsPage.getSize();
            if (size == 0) {
                ErrorDTOResponse error = new ErrorDTOResponse("No accounts found for user with ID: " + id, 404);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            PaginatedResponseDTO<AccountDTOResponse> paginatedResponse = accountMapper
                    .toPaginatedResponse(accountsPage);
            return ResponseEntity.ok(paginatedResponse);
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }

    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactionsForUser(@PathVariable Long id, @ModelAttribute TransactionFilter params) {
        try {
            if (id == null) {
                ErrorDTOResponse error = new ErrorDTOResponse("User ID cannot be null", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            if (params == null) {
                params = new TransactionFilter();
            }

            Page<Transaction> userTransactions = transactionsService.getUserTransactions(id, params);
            if (userTransactions.getSize() == 0) {
                ErrorDTOResponse error = new ErrorDTOResponse("No transactions found for user with ID: " + id, 404);
                return ResponseEntity.status(error.getCode()).body(error);
            }

            PaginatedResponseDTO<TransactionDTOResponse> paginatedResponse = transactionMapper
                    .toPaginatedResponse(userTransactions);

            return ResponseEntity.ok(paginatedResponse);
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

    @GetMapping()
    public List<UserDTO> getAllUsers(@RequestParam(required = false) Map<String, String> params) {
        UserFilter filter = new UserFilter();
        return userService.getAllUsers(params, filter);
    }

}
