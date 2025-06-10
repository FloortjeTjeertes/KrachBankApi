package com.krachbank.api.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.iban4j.Iban;
import org.iban4j.Iban4jException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;
import com.krachbank.api.service.TransactionService;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountService accountService;

    private Transaction fullTransaction;
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        transactionController = new TransactionController(transactionService, accountService);

        User initiator = new User();
        initiator.setId(1L);

        Account fromAccount = new Account();
        fromAccount.setIban(Iban.valueOf("DE89370400440532013000"));

        Account toAccount = new Account();
        toAccount.setIban(Iban.valueOf("DE12500105170648489890"));

        fullTransaction = new Transaction();
        fullTransaction.setAmount(BigDecimal.valueOf(100.0));
        fullTransaction.setCreatedAt(java.time.LocalDateTime.now());
        fullTransaction.setInitiator(initiator);
        fullTransaction.setFromAccount(fromAccount);
        fullTransaction.setToAccount(toAccount);
        fullTransaction.setDescription("Test transaction");
    }

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
        assertThrows(Iban4jException.class, () -> transactionController.toModel(dto));
    }

    @Test
    void testToModelWithNullDTO() {
        assertThrows(NullPointerException.class, () -> transactionController.toModel(null));
    }

    @Test
    void testGetTransactionsReturnsOkWithTransactions() {
        TransactionFilter filter = new TransactionFilter();
        Page<Transaction> transactions = new PageImpl<>(List.of(fullTransaction));
        List<TransactionDTOResponse> transactionDTOs = List.of(new TransactionDTOResponse());

        when(transactionService.getTransactionsByFilter(filter)).thenReturn(transactions);
        when(transactionService.toDTO(transactions.getContent())).thenReturn(transactionDTOs);

        ResponseEntity<?> response = transactionController.getTransactions(filter);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(transactionDTOs, response.getBody());
    }

    @Test
    void testGetTransactionsReturnsNoContentWhenEmpty() {
        TransactionFilter filter = new TransactionFilter();
        Page<Transaction> transactions = new PageImpl<>(List.of());

        when(transactionService.getTransactionsByFilter(filter)).thenReturn(transactions);

        ResponseEntity<?> response = transactionController.getTransactions(filter);

        // Should return 204 NO CONTENT if empty
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testGetTransactionsHandlesException() {
        TransactionFilter filter = new TransactionFilter();
        String errorMessage = "Database error";

        when(transactionService.getTransactionsByFilter(filter))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<?> response = transactionController.getTransactions(filter);

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals(errorMessage, response.getBody());
    }
}
