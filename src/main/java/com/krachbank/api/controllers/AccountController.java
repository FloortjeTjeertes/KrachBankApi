package com.krachbank.api.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.UserService;
import com.krachbank.api.models.User;

@RestController
@RequestMapping("/accounts")
public class AccountController implements Controller<Account, AccountDTOResponse, AccountDTORequest> {
    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
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
                    account.setAccountType(com.krachbank.api.models.AccountType.CHECKING);
                } else {
                    account.setAccountType(com.krachbank.api.models.AccountType.SAVINGS);
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
                accountDTOs.add(accountService.toDTO(account));
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            accountService.getAccountByIBAN(iban);

            return ResponseEntity.ok(toResponse(accountService.getAccountByIBAN(iban).get()));
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }

    }

    // @GetMapping()
    // @PreAuthorize("hasRole('ROLE_USER')")
    // public ResponseEntity<?> getAccountsForCurrentUser() {
    // try {

    // User user = (User)
    // SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // if (user == null) {
    // throw new Exception("User not found");
    // }
    // List<AccountDTO> accountDTOs = new ArrayList<AccountDTO>();
    // List<Account> accounts = accountService.getAccountsByUserId(null);
    // for (Account account : accounts) {
    // accountDTOs.add(accountService.toDTO(account));
    // }
    // return ResponseEntity.ok(accountDTOs);
    // } catch (Exception e) {
    // ErrorDTO error = new ErrorDTO(e.getMessage(), 500);
    // return ResponseEntity.status(error.getCode()).body(error);
    // }

    // }

    @GetMapping()
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAccounts(@ModelAttribute AccountFilter filter) {
        try {

            List<AccountDTOResponse> accountDTOs = new ArrayList<AccountDTOResponse>();
            Page<Account> accountsPage = accountService.getAccountsByFilter(filter);

            List<Account> accounts = accountsPage.getContent();

            if (accounts == null || accounts.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            accountDTOs = toResponseList(accounts);

            return ResponseEntity.ok(accountDTOs);
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }

    }

    @Override
    public Account toModel(AccountDTORequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("AccountDTOResponse cannot be null");
        }
        Account account = new Account();
        account.setIban(IBANGenerator.generateIBAN());
        account.setAccountType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setAbsoluteLimit(dto.getAbsoluteLimit());
        account.setTransactionLimit(dto.getTransactionLimit());
        account.setCreatedAt(LocalDateTime.now());

        return new Account();
    }

    // TODO: maybe make this generic
    public List<Account> toModelList(List<AccountDTORequest> dtoList) {
        List<Account> accounts = new ArrayList<>();
        for (AccountDTORequest dto : dtoList) {
            accounts.add(toModel(dto));
        }
        return accounts;
    }

    // TODO: maybe make this generic
    public List<AccountDTOResponse> toResponseList(List<Account> models) {
        List<AccountDTOResponse> dtos = new ArrayList<>();
        for (Account model : models) {
            dtos.add(toResponse(model));
        }
        return dtos;
    }

    @Override
    public AccountDTOResponse toResponse(Account model) {
        if (model == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        AccountDTOResponse dto = new AccountDTOResponse();
        dto.setIban(model.getIban().toString());
        dto.setBalance(model.getBalance());
        dto.setOwner(model.getUser().getId());
        dto.setAbsoluteLimit(model.getAbsoluteLimit());
        dto.setTransactionLimit(model.getTransactionLimit());
        dto.setType(model.getAccountType());
        dto.setCreatedAt(model.getCreatedAt() != null ? model.getCreatedAt().toString() : "Unknown");

        return dto;
    }

}
