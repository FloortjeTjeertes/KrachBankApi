package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException.NotImplemented;

import jakarta.persistence.criteria.Predicate;

import com.krachbank.api.dto.TransactionDTO;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.TransactionRepository;

@Service
public class TransactionJpa implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionJpa(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Optional<Transaction> createTransaction(TransactionDTO transactionDto) {
        Transaction transaction = toModel(transactionDto);

        try {
            isValidTransaction(transaction);
            transactionRepository.findById(transaction.getId()).ifPresent(existingTransaction -> {
                throw new IllegalArgumentException("Transaction already exists");
            });

            Account sendingAccount = transaction.getFromAccount();
            Account receivingAccount = transaction.getToAccount();

            // check if the transaction is to the same account
            if (sendingAccount.getIBAN().equals(receivingAccount.getIBAN())) {
                throw new IllegalArgumentException("cant transfer to the same account");

            }

            if (!receivingAccount.getUser().equals(sendingAccount.getUser())) {
                if (receivingAccount.getAccountType() == AccountType.SAVINGS
                        || sendingAccount.getAccountType() == AccountType.SAVINGS) {
                    throw new IllegalArgumentException("cant transfer money to or from another persons saving account");
                }

            }

            // calculate the resulting balance of the sending account
            // check if the amount is not more then the transaction limit (amount of money
            // that can be transferred per transaction)
            reachedAbsoluteLimit(sendingAccount, transaction.getAmount());
            reachedDailyTransferLimit(sendingAccount.getUser(), transaction.getAmount());

            Transaction savedTransaction = transactionRepository.save(transaction);

            //check if it saved correctly 
            

            return Optional.of(savedTransaction);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;

    }

    public Boolean reachedAbsoluteLimit(Account account, BigDecimal amount) throws Exception {
        BigDecimal resultingAmount = account.getBalance().subtract(amount);

        if (resultingAmount.compareTo(account.getAbsoluteLimit()) < 0) {
            throw new Exception("cant spend more then the absolute limit. in other words: you broke");

        }

        return true;
    }

    public Boolean reachedDailyTransferLimit(User user, BigDecimal amount) throws Exception {

        BigDecimal totalSpendBeforeToday = null; // total amount of money spend today
        BigDecimal totalSpendToday = totalSpendBeforeToday.add(amount);
        BigDecimal dailyLimit = user.getDailyLimit(); // users daily limit

        if (totalSpendToday.equals(dailyLimit) || totalSpendToday.compareTo(dailyLimit) < 0) {
            throw new Exception("dailylimit reached"); //

        }

        return true;
    }

    public Boolean transferAmountBiggerThenTransferLimit(Account account, BigDecimal amount) throws Exception {
        throw new Exception();

    }

    public BigDecimal getUserTotalAmountSpendAtDate(User user, LocalDateTime date) {
        List<Transaction> transactionsForUser = transactionRepository
                .findByInitiatorIdOrderByCreatedAtAsc(user.getId()); // change this to a service method if we ever get
                                                                     // one that retrieves all transactions for a single
                                                                     // user
        List<Transaction> filteredTransactions = transactionsForUser.stream()
                .filter(t -> t.getCreatedAt().toLocalDate().equals(date))
                .toList();

        return filteredTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {

        return transactionRepository.findById(id);
    }

    @Override
    public List<Transaction> getTransactionsByFilter(TransactionFilter filter) {

        return transactionRepository.findAll(MakeTransactionsSpecification(filter));

    }

    @Override
    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter) {
        return transactionRepository.findOne(MakeTransactionsSpecification(filter));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTransactions'");
    }

    @Override
    public Optional<Transaction> updateTransaction(Long id, TransactionDTO transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTransaction'");
    }

    public static Specification<Transaction> MakeTransactionsSpecification(TransactionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSenderId() != null) {
                predicates.add(cb.equal(root.get("fromAccount").get("id"), filter.getSenderId()));
            }
            if (filter.getReceiverId() != null) {
                predicates.add(cb.equal(root.get("toAccount").get("id"), filter.getReceiverId()));
            }
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            if (filter.getBeforeDate() != null) {
                predicates.add(cb.lessThan(root.get("date"), filter.getBeforeDate()));
            }
            if (filter.getAfterDate() != null) {
                predicates.add(cb.greaterThan(root.get("date"), filter.getAfterDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

    }

    public boolean isValidTransaction(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid transaction amount");
        }
        if (transaction.getFromAccount() == null || transaction.getToAccount() == null) {
            throw new IllegalArgumentException("Invalid transaction accounts");
        }
        if (transaction.getFromAccount().equals(transaction.getToAccount())) {
            throw new IllegalArgumentException("Transaction accounts must be different");
        }
        return true;
    }

    @Override
    public Transaction toModel(TransactionDTO dto) {

        User initUser = new User();
        initUser.setId(dto.getInitiator());

        Account fromAccount = new Account();

        Account receivingAccount = new Account();

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getAmount());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(receivingAccount);
        transaction.setInitiator(initUser);
        transaction.setCreatedAt(LocalDateTime.parse(dto.getCreatedAt()));
        transaction.setDescription(dto.getDescription());
        return transaction;

    }

    @Override
    public TransactionDTO toDTO(Transaction model) {
        // TODO Auto-generated method stub
        return model.toDTO();
    }

    @Override
    public List<TransactionDTO> toDTO(List<Transaction> transactions) {
        List<TransactionDTO> transactionDTOs = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionDTOs.add(transaction.toDTO());
        }
        return transactionDTOs;
    }

}
