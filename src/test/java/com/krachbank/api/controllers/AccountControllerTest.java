package com.krachbank.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.iban4j.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krachbank.api.dto.AccountDTORequest;
import com.krachbank.api.dto.AccountDTOResponse;
import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.PaginatedResponseDTO;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.dto.UserDTO;
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

class AccountControllerTest {

    @Mock
    private AccountService accountService;
    @Mock
    private UserService userService;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private AccountDTORequest buildAccountDTORequest(Long userId, BigDecimal balance, BigDecimal absLimit,
            BigDecimal txLimit) {
        AccountDTORequest req = new AccountDTORequest();
        req.setUserId(userId);
        req.setBalance(balance);
        req.setAbsoluteLimit(absLimit);
        req.setTransactionLimit(txLimit);
        return req;
    }

    // test buildUserDTO
    private UserDTO buildUserDTO(Long id) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setBSN("123456789");
        userDTO.setPhoneNumber("0612345678");
        return userDTO;
    }

    private Account buildAccount(Long userId, AccountType type) {
        Account acc = new Account();
        acc.setIban(Iban.valueOf("NL91ABNA0417164300"));
        acc.setCreatedAt(LocalDateTime.now());
        acc.setBalance(BigDecimal.TEN);
        acc.setAbsoluteLimit(BigDecimal.ONE);
        acc.setTransactionLimit(BigDecimal.ONE);
        acc.setAccountType(type);
        User user = new User();
        user.setId(userId);
        acc.setUser(user);
        return acc;
    }

    // Test cases for createAccounts method
    @Test
    void createAccounts_success() throws Exception {
        List<AccountDTORequest> requests = Arrays.asList(
                buildAccountDTORequest(1L, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE),
                buildAccountDTORequest(1L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        UserDTO userDTO = buildUserDTO(1L);
        when(userService.getUserById(1L)).thenReturn(userDTO);

        List<Account> createdAccounts = Arrays.asList(
                buildAccount(1L, AccountType.CHECKING),
                buildAccount(1L, AccountType.SAVINGS));
        when(accountService.createAccounts(anyList())).thenReturn(createdAccounts);

        AccountDTOResponse resp1 = new AccountDTOResponse();
        AccountDTOResponse resp2 = new AccountDTOResponse();
        when(accountMapper.toResponse(any(Account.class))).thenReturn(resp1, resp2);

        // Use MockMvc to perform the request
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        String json = new ObjectMapper().writeValueAsString(requests);

        mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));

        verify(userService, times(2)).getUserById(1L);
        verify(accountService).createAccounts(anyList());
        verify(accountMapper, times(2)).toResponse(any(Account.class));
    }

    @Test
    void createAccounts_missingUserId_returnsError() throws Exception {
        List<AccountDTORequest> requests = Collections.singletonList(
                buildAccountDTORequest(null, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        String json = new ObjectMapper().writeValueAsString(requests);

        mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Account owner is required"));
    }

    @Test
    void createAccounts_userServiceThrows_returnsError() throws Exception {
        List<AccountDTORequest> requests = Collections.singletonList(
                buildAccountDTORequest(99L, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE));
        when(userService.getUserById(99L)).thenThrow(new IllegalArgumentException("User not found"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        String json = new ObjectMapper().writeValueAsString(requests);

        mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not found"));
    }

    // get Account by Iban test cases

    @Test
    void getAccountByIban_success() throws Exception {
        String iban = "NL91ABNA0417164300";
        Account account = buildAccount(1L, AccountType.CHECKING);
        AccountDTOResponse dtoResponse = new AccountDTOResponse();

        when(accountService.getAccountByIBAN(iban)).thenReturn(java.util.Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(dtoResponse);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}", iban))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(accountService).getAccountByIBAN(iban);
        verify(accountMapper).toResponse(account);
    }

    @Test
    void getAccountByIban_blankIban_returnsError() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}", " "))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("IBAN is required"));
    }

    @Test
    void getAccountByIban_serviceThrows_returnsError() throws Exception {
        String iban = "NL91ABNA0417164300";
        when(accountService.getAccountByIBAN(iban)).thenThrow(new RuntimeException("Account not found"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}", iban))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Account not found"));
    }

    @Test
    void getAccountByIban_optionalEmpty_returnsError() throws Exception {
        String iban = "NL91ABNA0417164300";
        when(accountService.getAccountByIBAN(iban)).thenReturn(java.util.Optional.empty());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}", iban))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    // getTransactionsForAccount test cases

    @Test
    void getTransactionsForAccount_success() throws Exception {
        String iban = "NL91ABNA0417164300";
        TransactionFilter filter = new TransactionFilter();
        @SuppressWarnings("unchecked")
        Page<Transaction> transactionsPage = org.mockito.Mockito.mock(Page.class);
        PaginatedResponseDTO<TransactionDTOResponse> paginatedResponse = new PaginatedResponseDTO<>();

        when(transactionService.getTransactionsByIBAN(any(String.class), any(TransactionFilter.class)))
                .thenReturn(transactionsPage);
        when(transactionsPage.getSize()).thenReturn(1);
        when(transactionMapper.toPaginatedResponse(transactionsPage)).thenReturn(paginatedResponse);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}/transactions", iban))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(transactionService).getTransactionsByIBAN(any(String.class), any(TransactionFilter.class));
        verify(transactionMapper).toPaginatedResponse(transactionsPage);
    }


    @Test
    void getTransactionsForAccount_emptyIban_returnsError() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}/transactions", " "))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("IBAN is required"));
    }

    @Test
    void getTransactionsForAccount_noTransactions_returnsError() throws Exception {
        String iban = "NL91ABNA0417164300";
        @SuppressWarnings("unchecked")
        Page<Transaction> transactionsPage = org.mockito.Mockito.mock(Page.class);

        when(transactionService.getTransactionsByIBAN(any(String.class), any(TransactionFilter.class)))
                .thenReturn(transactionsPage);
        when(transactionsPage.getSize()).thenReturn(-1);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}/transactions", iban))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No transactions found for this account"));
    }

    @Test
    void getTransactionsForAccount_serviceThrows_returnsError() throws Exception {
        String iban = "NL91ABNA0417164300";
        when(transactionService.getTransactionsByIBAN(any(String.class), any(TransactionFilter.class)))
                .thenThrow(new RuntimeException("Service error"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts/{iban}/transactions", iban))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Service error"));
    }
    
    @

}