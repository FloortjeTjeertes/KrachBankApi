package com.krachbank.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.iban4j.Iban;
import org.iban4j.Iban4jException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.TransactionService;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Transaction fullTransaction;
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        // Mock or instantiate dependencies as needed

        transactionController = new TransactionController(transactionService, accountService);

        // Create a mock initiator (User)
        User initiator = new User();
        initiator.setId(1L);

        // Create mock accounts
        Account fromAccount = new Account();
        fromAccount.setIban(Iban.valueOf("DE89370400440532013000"));

        Account toAccount = new Account();
        toAccount.setIban(Iban.valueOf("DE12500105170648489890"));

        // Create a full transaction
        fullTransaction = new Transaction();
        fullTransaction.setAmount(BigDecimal.valueOf(100.0));
        fullTransaction.setCreatedAt(java.time.LocalDateTime.now());
        fullTransaction.setInitiator(initiator);
        fullTransaction.setFromAccount(fromAccount);
        fullTransaction.setToAccount(toAccount);
        fullTransaction.setDescription("Test transaction");
    }

 
    // TODO: move these to the test for the mapper class

    @Test
    void testToModelWithValidDTO() {
        TransactionDTOResponse dto = new TransactionDTOResponse();
        dto.setAmount(fullTransaction.getAmount());
        dto.setCreatedAt(fullTransaction.getCreatedAt());
        dto.setInitiator(fullTransaction.getInitiator().getId());
        dto.setSender(fullTransaction.getFromAccount().getIban().toString());
        dto.setReceiver(fullTransaction.getToAccount().getIban().toString());
        dto.setDescription(fullTransaction.getDescription());

        Transaction transaction = transactionController.toModel(dto);

        assertNotNull(transaction);
        assertEquals(fullTransaction.getAmount(), transaction.getAmount());
        assertEquals(fullTransaction.getCreatedAt(), transaction.getCreatedAt());
        assertEquals(fullTransaction.getDescription(), transaction.getDescription());
        assertNotNull(transaction.getInitiator());
        assertEquals(fullTransaction.getInitiator().getId(), transaction.getInitiator().getId());
        assertNotNull(transaction.getFromAccount());
        assertEquals(fullTransaction.getFromAccount().getIban(), transaction.getFromAccount().getIban());
        assertNotNull(transaction.getToAccount());
        assertEquals(fullTransaction.getToAccount().getIban(), transaction.getToAccount().getIban());
    }

    @Test
    void testToModelWithNullFields() {
        TransactionDTOResponse dto = new TransactionDTOResponse();
        // Only set initiator from setup
        assertThrows(Iban4jException.class, () -> transactionController.toModel(dto));

    }

    @Test
    void testToModelWithNullDTO() {
        assertThrows(NullPointerException.class, () -> transactionController.toModel(null));
    }

    @Test
    void testGetTransactionsReturnsOkWithTransactions() {
        TransactionFilter filter = new TransactionFilter();
        Page<Transaction> transactions = new org.springframework.data.domain.PageImpl<>(List.of(fullTransaction));
        List<TransactionDTOResponse> transactionDTOs = List.of(new TransactionDTOResponse());

        // Mock service behavior
        org.mockito.Mockito.when(transactionService.getTransactionsByFilter(filter)).thenReturn(transactions);
        org.mockito.Mockito.when(transactionService.toDTO(transactions.getContent())).thenReturn(transactionDTOs);

        ResponseEntity<?> response = transactionController.getTransactions(filter);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(transactionDTOs, response.getBody());
    }

    @Test
    void testGetTransactionsReturnsNullWhenEmpty() {
        TransactionFilter filter = new TransactionFilter();
        org.springframework.data.domain.Page<Transaction> transactions = new org.springframework.data.domain.PageImpl<>(List.of());

        org.mockito.Mockito.when(transactionService.getTransactionsByFilter(filter)).thenReturn(transactions);

        ResponseEntity<?> response = transactionController.getTransactions(filter);

        // According to implementation, should return null if empty
        assertEquals(null, response);
    }

    @Test
    void testGetTransactionsHandlesException() {
        TransactionFilter filter = new TransactionFilter();
        String errorMessage = "Database error";

        org.mockito.Mockito.when(transactionService.getTransactionsByFilter(filter))
            .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = transactionController.getTransactions(filter);

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals(errorMessage, response.getBody());
    }
}
