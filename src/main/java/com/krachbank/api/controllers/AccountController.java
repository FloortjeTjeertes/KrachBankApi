package com.krachbank.api.controllers;

transactionsadmin
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final UserMapper userMapper; // Assuming you have a UserMapper similar to AccountMapper

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
    public ResponseEntity<?> createAccounts(@RequestBody List<AccountDTORequest> accountRequests) {
        try {
            List<Account> accounts = new ArrayList<>();
            for (int i = 0; i < accountRequests.size(); i++) {
                AccountDTORequest accountRequest = accountRequests.get(i);
                // Account account = new Account();
                // account.setIban(IBANGenerator.generateIBAN());
                // account.setCreatedAt(LocalDateTime.now());
                // account.setBalance(accountRequest.getBalance() != null &&
                // accountRequest.getBalance().compareTo(BigDecimal.ZERO) != 0 ?
                // accountRequest.getBalance() : BigDecimal.ZERO);
                // account.setAbsoluteLimit(accountRequest.getAbsoluteLimit() != null &&
                // accountRequest.getAbsoluteLimit().compareTo(BigDecimal.ZERO) != 0 ?
                // accountRequest.getAbsoluteLimit() : BigDecimal.ZERO);
                // account.setTransactionLimit(accountRequest.getTransactionLimit() != null &&
                // accountRequest.getTransactionLimit().compareTo(BigDecimal.ZERO) != 0 ?
                // accountRequest.getTransactionLimit() : BigDecimal.ZERO);

                Account account = new Account();
                account.setIban(IBANGenerator.generateIBAN());
                account.setCreatedAt(LocalDateTime.now());
                account.setBalance(
                        accountRequest.getBalance() != null
                                && accountRequest.getBalance().compareTo(java.math.BigDecimal.ZERO) != 0
                                ? accountRequest.getBalance()
                                : java.math.BigDecimal.ZERO);
                account.setAbsoluteLimit(
                        accountRequest.getAbsoluteLimit() != null
                                && accountRequest.getAbsoluteLimit().compareTo(java.math.BigDecimal.ZERO) != 0
                                ? accountRequest.getAbsoluteLimit()
                                : java.math.BigDecimal.ZERO);
                account.setTransactionLimit(
                        accountRequest.getTransactionLimit() != null
                                && accountRequest.getTransactionLimit().compareTo(java.math.BigDecimal.ZERO) != 0
                                ? accountRequest.getTransactionLimit()
                                : java.math.BigDecimal.ZERO);
                account = accountMapper.toModel(accountRequest);
                // Set account type: first is CHECKINGS, second is SAVINGS

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
                // // set userdto to User
                // User user = new User();
                // user.setId(userDTO.getId());
                // user.setEmail(userDTO.getEmail());
                // user.setFirstName(userDTO.getFirstName());
                // user.setLastName(userDTO.getLastName());
                // user.setBSN(userDTO.getBSN());
                // user.setPhoneNumber(userDTO.getPhoneNumber());
                User user = userMapper.toModel(userDTO);

                account.setUser(user);
                // --- end set owner ---
                accounts.add(account);
            }
            List<AccountDTOResponse> accountDTOs = new ArrayList<>();
            List<Account> returnAccounts = accountService.createAccounts(accounts);
            for (Account account : returnAccounts) {
                accountDTOs.add(accountMapper.toResponse(account));
            }
            return ResponseEntity.ok(accountDTOs);
        } catch (IllegalArgumentException e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }
    }

    @GetMapping("/{iban}")
    public ResponseEntity<?> getAccountByIban(@PathVariable String iban) {
        try {
            if (iban.isBlank() || iban.isEmpty()) {
                ErrorDTOResponse error = new ErrorDTOResponse("IBAN is required", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            // Ensure getAccountByIBAN returns an Optional and handle it
            return ResponseEntity.ok(accountMapper.toResponse(accountService.getAccountByIBAN(iban).orElseThrow(
                    () -> new IllegalArgumentException("Account not found with IBAN: " + iban)
            )));
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
            if (iban.isEmpty() || iban.isBlank()) {
                ErrorDTOResponse error = new ErrorDTOResponse("IBAN is required", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            if (filter == null) {
                filter = new TransactionFilter();
            }
            Page<Transaction> transactionsPage = transactionService.getTransactionsByIBAN(iban, filter);
            if (transactionsPage.getSize() < 0) { // Should be transactionsPage.isEmpty() or transactionsPage.getTotalElements() == 0
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

            if (accountsPage.getSize() < 0) { // Should be accountsPage.isEmpty() or accountsPage.getTotalElements() == 0
                ErrorDTOResponse error = new ErrorDTOResponse("No accounts found", 404);
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

    // UPDATED to use POST mapping with a more specific endpoint
    @PostMapping("/{iban}/transaction-limit") // Changed to @PostMapping and added specific sub-path
    public ResponseEntity<?> updateAccountTransactionLimit(@PathVariable String iban, @RequestBody Map<String, Object> updates) {
        try {
            if (iban == null || iban.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorDTOResponse("IBAN is required for update", 400));
            }

            BigDecimal newLimit = null;
            if (updates.containsKey("transactionLimit")) {
                Object limitObj = updates.get("transactionLimit");
                if (limitObj instanceof Number) {
                    newLimit = BigDecimal.valueOf(((Number) limitObj).doubleValue());
                } else if (limitObj instanceof String) {
                    try {
                        newLimit = new BigDecimal((String) limitObj);
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest().body(new ErrorDTOResponse("Invalid number format for transactionLimit", 400));
                    }
                } else {
                    return ResponseEntity.badRequest().body(new ErrorDTOResponse("Invalid format for transactionLimit. Expected Number or String.", 400));
                }
            } else {
                return ResponseEntity.badRequest().body(new ErrorDTOResponse("transactionLimit field is missing in the request body", 400));
            }

            Optional<Account> optionalAccount = accountService.getAccountByIBAN(iban); // Uses the path variable 'iban'
            if (optionalAccount.isEmpty()) {
                return ResponseEntity.status(404).body(new ErrorDTOResponse("Account not found with IBAN: " + iban, 404));
            }
            Account accountToUpdate = optionalAccount.get();
            logger.debug("Controller: Account retrieved by IBAN has ID: {}", accountToUpdate.getId());

            accountToUpdate.setTransactionLimit(newLimit);

            Account updatedAccount = accountService.updateAccount(accountToUpdate.getId(), accountToUpdate).orElseThrow(
                    () -> new RuntimeException("Failed to update account with IBAN: " + iban)
            );

            return ResponseEntity.ok(accountMapper.toResponse(updatedAccount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorDTOResponse(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorDTOResponse(e.getMessage(), 500));
        }
    }
}
}
