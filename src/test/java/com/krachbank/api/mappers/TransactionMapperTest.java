package com.krachbank.api.mappers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.iban4j.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.dto.TransactionDTORequest;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.service.AccountService;





class TransactionMapperTest {

    private AccountService accountService;
    private TransactionMapper transactionMapper;


    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        transactionMapper = new TransactionMapper(accountService);
    }

    @Test
    void testToModel_mapsDtoToModelCorrectly() {
        // Arrange
        String senderIban = "DE32500211205487556354";
        String receiverIban = "DE52500202006796187625";
        Long initiatorId = 42L;
        String description = "Test transaction";
        BigDecimal amount = new BigDecimal("100.50");

        TransactionDTORequest dto = new TransactionDTORequest();
        dto.setSender(senderIban);
        dto.setReceiver(receiverIban);
        dto.setInitiator(initiatorId);
        dto.setDescription(description);
        dto.setAmount(amount);

        Account senderAccount = new Account();
        senderAccount.setIban(Iban.valueOf(senderIban));

        Account receiverAccount = new Account();
        receiverAccount.setIban(Iban.valueOf(receiverIban));

        when(accountService.getAccountByIBAN(senderIban)).thenReturn(Optional.of(senderAccount));
        when(accountService.getAccountByIBAN(receiverIban)).thenReturn(Optional.of(receiverAccount));

        // Act
        Transaction transaction = transactionMapper.toModel(dto);

        // Assert
        assertNotNull(transaction);
        assertEquals(senderAccount, transaction.getFromAccount());
        assertEquals(receiverAccount, transaction.getToAccount());
        assertNotNull(transaction.getInitiator());
        assertEquals(initiatorId, transaction.getInitiator().getId());
        assertEquals(description, transaction.getDescription());
        assertEquals(amount, transaction.getAmount());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void testToResponse_mapsModelToResponseCorrectly() {
        // Arrange
        String senderIban = "DE32500211205487556354";
        String receiverIban = "DE52500202006796187625";
        Long initiatorId = 42L;
        String description = "Test transaction";
        BigDecimal amount = new BigDecimal("100.50");
        LocalDateTime createdAt = LocalDateTime.now();

        Account senderAccount = new Account();
        senderAccount.setIban(Iban.valueOf(senderIban));

        Account receiverAccount = new Account();
        receiverAccount.setIban(Iban.valueOf(receiverIban));

        User initiator = new User();
        initiator.setId(initiatorId);

        Transaction transaction = new Transaction();
        transaction.setFromAccount(senderAccount);
        transaction.setToAccount(receiverAccount);
        transaction.setInitiator(initiator);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setCreatedAt(createdAt);

        // Act
        TransactionDTOResponse response = transactionMapper.toResponse(transaction);

        // Assert
        assertNotNull(response);
        assertEquals(senderIban, response.getSender());
        assertEquals(receiverIban, response.getReceiver());
        assertEquals(initiatorId, response.getInitiator());
        assertEquals(description, response.getDescription());
        assertEquals(amount, response.getAmount());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToModel_throwsExceptionWhenSenderAccountNotFound() {
        TransactionDTORequest dto = new TransactionDTORequest();
        dto.setSender("NOT_FOUND");
        dto.setReceiver("RECEIVER_IBAN");
        dto.setInitiator(1L);
        dto.setDescription("desc");
        dto.setAmount(BigDecimal.ONE);

        when(accountService.getAccountByIBAN("NOT_FOUND")).thenReturn(Optional.empty());
        when(accountService.getAccountByIBAN("RECEIVER_IBAN")).thenReturn(Optional.of(new Account()));

        assertThrows(java.util.NoSuchElementException.class, () -> transactionMapper.toModel(dto));
    }

    @Test
    void testToModel_throwsExceptionWhenReceiverAccountNotFound() {
        TransactionDTORequest dto = new TransactionDTORequest();
        dto.setSender("SENDER_IBAN");
        dto.setReceiver("NOT_FOUND");
        dto.setInitiator(1L);
        dto.setDescription("desc");
        dto.setAmount(BigDecimal.ONE);

        when(accountService.getAccountByIBAN("SENDER_IBAN")).thenReturn(Optional.of(new Account()));
        when(accountService.getAccountByIBAN("NOT_FOUND")).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> transactionMapper.toModel(dto));
    }
}