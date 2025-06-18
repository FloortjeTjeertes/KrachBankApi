package com.krachbank.api.controllers;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krachbank.api.dto.ErrorDTOResponse;
import com.krachbank.api.dto.PaginatedResponseDTO;
import com.krachbank.api.dto.TransactionDTORequest;
import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.mappers.TransactionMapper;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.service.TransactionService;


@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @GetMapping
    public ResponseEntity<?> getTransactions(@ModelAttribute TransactionFilter filter) {
        try {
            if (filter == null) {
                filter = new TransactionFilter();
            }
            Page<Transaction> transactionPage = transactionService.getTransactionsByFilter(filter);
            if (transactionPage.getSize() == 0) {
                ErrorDTOResponse error = new ErrorDTOResponse("No transactions found", 404);
                return ResponseEntity.status(error.getCode()).body(error.getMessage());
            }
            PaginatedResponseDTO<TransactionDTOResponse> paginatedResponse = transactionMapper.toPaginatedResponse(transactionPage);
            return ResponseEntity.ok(paginatedResponse);

        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }

    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody TransactionDTORequest transactionDTO) {
        try {
            Transaction transaction = transactionMapper.toModel(transactionDTO);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Transaction> createdTransaction = transactionService.createTransaction(transaction,username);


            if (createdTransaction.isPresent()) {
                return ResponseEntity.ok(transactionMapper.toResponse(createdTransaction.get()));

            } else {
                throw new Exception("transaction did not safe right");
            }
        } catch (Exception e) {
            ErrorDTOResponse error = new ErrorDTOResponse(e.getMessage(), 500);
            return ResponseEntity.status(error.getCode()).body(error);
        }
    }


   

 

   
}
