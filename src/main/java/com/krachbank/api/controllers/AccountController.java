package com.krachbank.api.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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


    public AccountController(AccountService accountService, UserService userService, AccountMapper accountMapper,
            TransactionService transactionService, TransactionMapper transactionMapper) {
        this.accountService = accountService;
        this.userService = userService;
        this.accountMapper = accountMapper;
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping
    public ResponseEntity<?> createAccounts(@RequestBody List<AccountDTORequest> accountRequests) {
        try {
            List<Account> accounts = new ArrayList<>();
            for (int i = 0; i < accountRequests.size(); i++) {
                AccountDTORequest accountRequest = accountRequests.get(i);
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
                // Fetch the user entity by userId (expects Long)
                UserDTO userDTO = userService.getUserById(accountRequest.getUserId());
                // set userdto to User
                User user = new User();
                user.setId(userDTO.getId());
                user.setEmail(userDTO.getEmail());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setBSN(userDTO.getBSN());
                user.setPhoneNumber(userDTO.getPhoneNumber());

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
            if (iban == null || iban.isEmpty()) {
                ErrorDTOResponse error = new ErrorDTOResponse("IBAN is required", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            accountService.getAccountByIBAN(iban);

            return ResponseEntity.ok(accountMapper.toResponse(accountService.getAccountByIBAN(iban).get()));
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }

    }

    @GetMapping("/{iban}/transactions")
    public ResponseEntity<?> getTransactionsForAccount(@PathVariable String iban,@ModelAttribute TransactionFilter filter) {
        try {
            if (iban == null || iban.isEmpty()) {
                ErrorDTOResponse error = new ErrorDTOResponse("IBAN is required", 400);
                return ResponseEntity.status(error.getCode()).body(error);
            }
            if (filter == null) {
                filter = new TransactionFilter();
            }
            Page<Transaction> transactionsPage = transactionService.getTransactionsByIBAN(iban, filter);
            if (transactionsPage.getSize() < 0) {
                ErrorDTOResponse error = new ErrorDTOResponse("No transactions found for this account", 404);
                return ResponseEntity.status(error.getCode()).body(error);
            }

            PaginatedResponseDTO<TransactionDTOResponse> paginatedResponse = transactionMapper.toPaginatedResponse(transactionsPage);

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

            if (accountsPage.getSize() < 0) {
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


}
