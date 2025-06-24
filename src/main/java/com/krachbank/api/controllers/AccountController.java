package com.krachbank.api.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PatchMapping; // No longer needed if not using PATCH
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Keep this for POST
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.configuration.IBANGenerator;
import com.krachbank.api.dto.AccountDTORequest;
import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.PaginatedResponseDTO;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.mappers.AccountMapper;
import com.krachbank.api.mappers.TransactionMapper;
import com.krachbank.api.mappers.UserMapper;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.TransactionService;
import com.krachbank.api.service.UserService;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    private final UserMapper userMapper;

    public AccountController(AccountService accountService, UserService userService, AccountMapper accountMapper,
                             TransactionService transactionService, TransactionMapper transactionMapper, UserMapper userMapper) {
        this.accountService = accountService;
        this.userService = userService;
        this.accountMapper = accountMapper;
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
        this.userMapper = userMapper;
    }

    @PostMapping
    public ResponseEntity<?> createAccounts(@RequestBody List<AccountDTORequest> accountRequests) throws Exception {
        try {
            List<Account> accounts = new ArrayList<>();
            for (int i = 0; i < accountRequests.size(); i++) {
                AccountDTORequest accountRequest = accountRequests.get(i);

                Account account = accountMapper.toModel(accountRequest);
                if (i == 0) {
                    account.setAccountType(AccountType.CHECKING);
                } else {
                    account.setAccountType(AccountType.SAVINGS);
                }
                // --- Set the account owner ---
                if (accountRequest.getUserId() == null) {
                    throw new IllegalArgumentException("Account owner is required");
                }
                // // Fetch the user entity by userId (expects Long)
                UserDTO userDTO = userService.getUserById(accountRequest.getUserId());
                User user = userMapper.toModel(userDTO);

                account.setUser(user);
                accounts.add(account);
            }
            List<Account> returnAccounts = accountService.createAccounts(accounts);
            List<AccountDTOResponse> accountDTOs = accountMapper.toResponseList(returnAccounts);

            return ResponseEntity.ok(accountDTOs);
        } catch (IllegalArgumentException e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }
    }

    @GetMapping("/{iban}")
    public ResponseEntity<?> getAccountByIban(@PathVariable String iban) {
        try {
            if (iban.isBlank()) {
                ErrorDTOResponse error = new ErrorDTOResponse("IBAN is required", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            // Ensure getAccountByIBAN returns an Optional and handle it
            return ResponseEntity.ok(accountMapper.toResponse(accountService.getAccountByIBAN(iban).orElseThrow(
                    () -> new IllegalArgumentException("Account not found with IBAN: " + iban))));
        } catch (IllegalArgumentException e) { // Catch specific exception for not found account
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 404); // 404 for not found
            return ResponseEntity.status(error.getCode()).body(error);
        } catch (Exception e) { // Catch other general exceptions
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }
    }

    @GetMapping("/{iban}/transactions")
    public ResponseEntity<?> getTransactionsForAccount(@PathVariable String iban,
                                                       @ModelAttribute TransactionFilter filter) {
        try {
            if (iban.isBlank()) {
                ErrorDTOResponse error = new ErrorDTOResponse("IBAN is required", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            if (filter == null) {
                filter = new TransactionFilter();
            }
            Page<Transaction> transactionsPage = transactionService.getTransactionsByIBAN(iban, filter);
            if (transactionsPage.getSize() < 0) { // Should be transactionsPage.isEmpty() or
                // transactionsPage.getTotalElements() == 0
                ErrorDTOResponse error = new ErrorDTOResponse("No transactions found for this account", 404);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            PaginatedResponseDTO<TransactionDTOResponse> paginatedResponse = transactionMapper
                    .toPaginatedResponse(transactionsPage);
            return ResponseEntity.ok(paginatedResponse);
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }
    }

    @GetMapping()
    public ResponseEntity<?> getAccounts(@ModelAttribute AccountFilter filter) {
        try {

            if (filter == null) {
                filter = new AccountFilter();
            }
            Page<Account> accountsPage = accountService.getAccountsByFilter(filter);

            if (accountsPage.getSize() < 0) { // Should be accountsPage.isEmpty() or accountsPage.getTotalElements() ==
                // 0
                ErrorDTOResponse error = new ErrorDTOResponse("No accounts found", 404);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            PaginatedResponseDTO<AccountDTOResponse> paginatedResponse = accountMapper
                    .toPaginatedResponse(accountsPage);
            return ResponseEntity.ok(paginatedResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorDTOResponse(e.getMessage(), 500));
        }
    }

    // UPDATED: Renamed method to be more generic and handle both transaction and absolute limits
    // Changed mapping to a more general POST endpoint for limit updates
    @PostMapping("/{iban}/limits")
    public ResponseEntity<?> updateAccountLimits(@PathVariable String iban,
                                                 @RequestBody Map<String, Object> updates) {
        try {
            if (iban == null || iban.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorDTOResponse("IBAN is required for update", 400));
            }

            Optional<Account> optionalAccount = accountService.getAccountByIBAN(iban);
            if (optionalAccount.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(new ErrorDTOResponse("Account not found with IBAN: " + iban, 404));
            }
            Account accountToUpdate = optionalAccount.get();

            // Handle transactionLimit update
            if (updates.containsKey("transactionLimit")) {
                Object limitObj = updates.get("transactionLimit");
                BigDecimal newTransactionLimit;
                if (limitObj instanceof Number) {
                    newTransactionLimit = BigDecimal.valueOf(((Number) limitObj).doubleValue());
                } else if (limitObj instanceof String) {
                    try {
                        newTransactionLimit = new BigDecimal((String) limitObj);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest()
                                .body(new ErrorDTOResponse("Invalid number format for transactionLimit", 400));
                    }
                } else {
                    return ResponseEntity.badRequest().body(new ErrorDTOResponse(
                            "Invalid format for transactionLimit. Expected Number or String.", 400));
                }
                // Basic validation: transactionLimit must be non-negative
                if (newTransactionLimit.compareTo(BigDecimal.ZERO) < 0) {
                    return ResponseEntity.badRequest()
                            .body(new ErrorDTOResponse("Transaction limit cannot be negative.", 400));
                }
                accountToUpdate.setTransactionLimit(newTransactionLimit);
            }

            // Handle absoluteLimit update
            if (updates.containsKey("absoluteLimit")) {
                Object limitObj = updates.get("absoluteLimit");
                BigDecimal newAbsoluteLimit;
                if (limitObj instanceof Number) {
                    newAbsoluteLimit = BigDecimal.valueOf(((Number) limitObj).doubleValue());
                } else if (limitObj instanceof String) {
                    try {
                        newAbsoluteLimit = new BigDecimal((String) limitObj);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest()
                                .body(new ErrorDTOResponse("Invalid number format for absoluteLimit", 400));
                    }
                } else {
                    return ResponseEntity.badRequest().body(new ErrorDTOResponse(
                            "Invalid format for absoluteLimit. Expected Number or String.", 400));
                }
                // Validation: absoluteLimit must be 0 or lower (i.e., less than or equal to 0)
                if (newAbsoluteLimit.compareTo(BigDecimal.ZERO) > 0) {
                    return ResponseEntity.badRequest()
                            .body(new ErrorDTOResponse("Absolute limit must be 0 or lower.", 400));
                }
                accountToUpdate.setAbsoluteLimit(newAbsoluteLimit); // Assuming Account model has setAbsoluteLimit
            }

            // If neither limit was provided, return a bad request
            if (!updates.containsKey("transactionLimit") && !updates.containsKey("absoluteLimit")) {
                return ResponseEntity.badRequest()
                        .body(new ErrorDTOResponse("No limit fields provided for update.", 400));
            }


            Account updatedAccount = accountService.updateAccount(accountToUpdate.getId(), accountToUpdate).orElseThrow(
                    () -> new RuntimeException("Failed to update account with IBAN: " + iban));

            return ResponseEntity.ok(accountMapper.toResponse(updatedAccount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorDTOResponse(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorDTOResponse(e.getMessage(), 500));
        }
    }
}
