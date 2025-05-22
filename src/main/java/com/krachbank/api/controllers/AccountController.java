package com.krachbank.api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.configuration.IBANGenerator;
import com.krachbank.api.dto.AccountDTO;
import com.krachbank.api.models.Account;
import com.krachbank.api.service.AccountService;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public List<AccountDTO> createAccounts(List<Account> accounts) {
        try {
            for (Account account : accounts) {
                account.setIBAN(IBANGenerator.generateIBAN());
            }
            List<AccountDTO> accountDTOs = new ArrayList<AccountDTO>();
            List<Account> returnAccounts = accountService.createAccounts(accounts);
            for (Account account : returnAccounts) {
                accountDTOs.add(accountService.toDTO(account));
            }
            return accountDTOs;
        } catch (IllegalArgumentException e) {
            // Handle the exception as needed, e.g., log it or return an error response
            System.out.println("Error creating accounts: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }

}
