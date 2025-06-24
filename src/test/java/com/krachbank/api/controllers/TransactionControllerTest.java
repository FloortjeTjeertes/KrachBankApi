package com.krachbank.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.PaginatedResponseDTO;
import com.krachbank.api.dto.TransactionDTORequest;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.mappers.TransactionMapper;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.service.TransactionService;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private Transaction fullTransaction;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder.class);

    }

    @AfterEach
    void tearDownSecurityContext() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
    }

    // Test for getTransactions method
    @Test
    void getTransactions_returnsPaginatedResponse_whenTransactionsExist() throws Exception {
        Transaction transaction = new Transaction();
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction));
        PaginatedResponseDTO<TransactionDTOResponse> paginatedResponse = new PaginatedResponseDTO<>();

        when(transactionService.getTransactionsByFilter(any(TransactionFilter.class))).thenReturn(transactionPage);
        when(transactionMapper.toPaginatedResponse(transactionPage)).thenReturn(paginatedResponse);

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(paginatedResponse)));
    }

    @Test
    void getTransactions_returnsNotFound_whenNoTransactionsExist() throws Exception {
        Page<Transaction> emptyPage = new PageImpl<>(List.of());

        when(transactionService.getTransactionsByFilter(any(TransactionFilter.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No transactions found"));
    }

    @Test
    void getTransactions_returnsInternalServerError_onException() throws Exception {
        when(transactionService.getTransactionsByFilter(any(TransactionFilter.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"message\":\"Database error\",\"code\":500}"));
    }

    // test createTransaction method

    @Test
    void createTransaction_returnsOk_whenTransactionCreated() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        TransactionDTORequest dtoRequest = new TransactionDTORequest();
        Transaction transaction = new Transaction();
        Transaction createdTransaction = new Transaction();
        TransactionDTOResponse dtoResponse = new TransactionDTOResponse();

        when(transactionMapper.toModel(any(TransactionDTORequest.class))).thenReturn(transaction);
        when(transactionService.createTransaction(eq(transaction), eq("testuser")))
                .thenReturn(Optional.of(createdTransaction));
        when(transactionMapper.toResponse(createdTransaction)).thenReturn(dtoResponse);

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dtoResponse)));
    }

    @Test
    void createTransaction_returnsInternalServerError_whenTransactionNotSaved() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        TransactionDTORequest dtoRequest = new TransactionDTORequest();
        Transaction transaction = new Transaction();

        when(transactionMapper.toModel(any(TransactionDTORequest.class))).thenReturn(transaction);
        when(transactionService.createTransaction(eq(transaction), eq("testuser"))).thenReturn(Optional.empty());

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(new ErrorDTOResponse("transaction did not safe right", 500))));
    }

    @Test
    void createTransaction_returnsInternalServerError_onException() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        TransactionDTORequest dtoRequest = new TransactionDTORequest();
        Transaction transaction = new Transaction();

        when(transactionMapper.toModel(any(TransactionDTORequest.class))).thenReturn(transaction);
        when(transactionService.createTransaction(eq(transaction), eq("testuser")))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"message\":\"Database error\",\"code\":500}"));
    }
}