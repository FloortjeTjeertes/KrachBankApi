package com.krachbank.api.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.configuration.IBANGenerator;
import com.krachbank.api.dto.AccountDTORequest;
import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.filters.AccountFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.service.AccountService;

@RestController
@RequestMapping("/accounts")
public class AccountController implements Controller<Account, AccountDTOResponse> {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<?> createAccounts(List<AccountDTORequest> accountRequests) {
        try {
            for (AccountDTORequest accountRequest : accountRequests) {
                accountRequest.setIban(IBANGenerator.generateIBAN());
            }
            List<AccountDTOResponse> accountDTOs = new ArrayList<AccountDTOResponse>();
            List<Account> accounts = toModelList(accountDTOs);

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if(){

            }

            accountService.getAccountByIBAN(iban);

            return ResponseEntity.ok(accountService.toDTO(accountService.getAccountByIBAN(iban).get()));
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }

    }

    // @GetMapping()
    // @PreAuthorize("hasRole('ROLE_USER')")
    // public ResponseEntity<?> getAccountsForCurrentUser() {
    //     try {

    //         User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //         if (user == null) {
    //             throw new Exception("User not found");
    //         }
    //         List<AccountDTO> accountDTOs = new ArrayList<AccountDTO>();
    //         List<Account> accounts = accountService.getAccountsByUserId(null);
    //         for (Account account : accounts) {
    //             accountDTOs.add(accountService.toDTO(account));
    //         }
    //         return ResponseEntity.ok(accountDTOs);
    //     } catch (Exception e) {
    //         ErrorDTO error = new ErrorDTO(e.getMessage(), 500);
    //         return ResponseEntity.status(error.getCode()).body(error);
    //     }

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
    public Account toModel(AccountDTOResponse dto) {
        if (dto == null) {
            throw new IllegalArgumentException("AccountDTOResponse cannot be null");
        }
        Account account = new Account();
        account.setIban(IBANGenerator.generateIBAN());
        account.setAccountType(dto.getType());
        account.setBalance(dto.getBalance());
        account.setAbsoluteLimit(dto.getAbsoluteLimit());
        account.setTransactionLimit(dto.getTransactionLimit());
        account.setCreatedAt(LocalDateTime.parse(dto.getCreatedAt()));

        return new Account();
    }

    public List<Account> toModelList(List<AccountDTOResponse> dtoList) {
        List<Account> accounts = new ArrayList<>();
        for (AccountDTOResponse dto : dtoList) {
            accounts.add(toModel(dto));
        }
        return accounts;
    }



}
