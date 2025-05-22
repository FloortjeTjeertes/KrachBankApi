package com.krachbank.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

import org.iban4j.Iban;
import org.iban4j.Iban4jException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.krachbank.api.dto.TransactionDTO;
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
        fromAccount.setIBAN(Iban.valueOf("DE89370400440532013000"));

        Account toAccount = new Account();
        toAccount.setIBAN(Iban.valueOf("DE12500105170648489890"));

        // Create a full transaction
        fullTransaction = new Transaction();
        fullTransaction.setAmount(BigDecimal.valueOf(100.0));
        fullTransaction.setCreatedAt(java.time.LocalDateTime.now());
        fullTransaction.setInitiator(initiator);
        fullTransaction.setFromAccount(fromAccount);
        fullTransaction.setToAccount(toAccount);
        fullTransaction.setDescription("Test transaction");
    }

    @Test
    void testCreateTransaction() {

    }

    @Test
    void testGetTransactions() {

    }
    // TODO: move these to the test for the mapper class

    @Test
    void testToModelWithValidDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(fullTransaction.getAmount());
        dto.setCreatedAt(fullTransaction.getCreatedAt());
        dto.setInitiator(fullTransaction.getInitiator().getId());
        dto.setSender(fullTransaction.getFromAccount().getIBAN().toString());
        dto.setReceiver(fullTransaction.getToAccount().getIBAN().toString());
        dto.setDescription(fullTransaction.getDescription());

        Transaction transaction = transactionController.toModel(dto);

        assertNotNull(transaction);
        assertEquals(fullTransaction.getAmount(), transaction.getAmount());
        assertEquals(fullTransaction.getCreatedAt(), transaction.getCreatedAt());
        assertEquals(fullTransaction.getDescription(), transaction.getDescription());
        assertNotNull(transaction.getInitiator());
        assertEquals(fullTransaction.getInitiator().getId(), transaction.getInitiator().getId());
        assertNotNull(transaction.getFromAccount());
        assertEquals(fullTransaction.getFromAccount().getIBAN(), transaction.getFromAccount().getIBAN());
        assertNotNull(transaction.getToAccount());
        assertEquals(fullTransaction.getToAccount().getIBAN(), transaction.getToAccount().getIBAN());
    }

    @Test
    void testToModelWithNullFields() {
        TransactionDTO dto = new TransactionDTO();
        // Only set initiator from setup
        assertThrows(Iban4jException.class, () -> transactionController.toModel(dto));

    }

    @Test
    void testToModelWithNullDTO() {
        assertThrows(NullPointerException.class, () -> transactionController.toModel(null));
    }
}
