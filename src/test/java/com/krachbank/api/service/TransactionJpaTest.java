package com.krachbank.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.iban4j.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.TransactionRepository;

public class TransactionJpaTest {
    
    TransactionService transactionService;
    TransactionRepository transactionRepository;

    Transaction fullTransaction;

    @BeforeEach
    void setUp(){
        transactionRepository = mock(TransactionRepository.class);
        transactionService = new TransactionJpa(transactionRepository);

        fullTransaction= new Transaction();
        fullTransaction.setId(1L);
        fullTransaction.setAmount(new BigDecimal("100.00"));
        fullTransaction.setDescription("Test transaction");
        fullTransaction.setCreatedAt(LocalDateTime.now());

        Iban iban = Iban.valueOf("NL91ABNA0417164300");
        Iban iban2 = Iban.valueOf("NL64ABNA0417164301");


        Account fromAccount = new Account();
        fromAccount.setId(10L);
        fullTransaction.setFromAccount(fromAccount);

        Account toAccount = new Account();
        toAccount.setIBAN(iban);
        toAccount.setId(20L);
        fullTransaction.setToAccount(toAccount);

        User initiator = new User();
        toAccount.setIBAN(iban2);
        initiator.setId(100L);
        fullTransaction.setInitiator(initiator);


    }

    @Test
    void testToDTOWithFullTransaction() {
       

        TransactionJpa transactionJpa = new TransactionJpa(null);
        TransactionDTO dto = transactionJpa.toDTO(fullTransaction);

        assertNotNull(dto);
        assertEquals(fullTransaction.getAmount(), dto.getAmount());
        assertEquals(fullTransaction.getDescription(), dto.getDescription());
        assertEquals(fullTransaction.getCreatedAt().toString(), dto.getCreatedAt());
        assertEquals(fullTransaction.getFromAccount().getId(), dto.getSender());
        assertEquals(fullTransaction.getToAccount().getId(), dto.getReceiver());
        assertEquals(fullTransaction.getInitiator().getId(), dto.getInitiator());
    }

    @Test
    void testToDTOWithNullTransaction() {
        TransactionJpa transactionJpa = new TransactionJpa(null);
        assertThrows(NullPointerException.class, () -> transactionJpa.toDTO((Transaction) null));
    }
}