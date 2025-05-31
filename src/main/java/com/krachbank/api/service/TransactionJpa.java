package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.krachbank.api.dto.TransactionDTOResponse;
import com.krachbank.api.filters.BaseFilter;
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.TransactionRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

@Service
public class TransactionJpa implements TransactionService {

    private final AccountServiceJpa accountServiceJpa;

    private final TransactionRepository transactionRepository;

    public TransactionJpa(TransactionRepository transactionRepository, AccountServiceJpa accountServiceJpa) {
        this.transactionRepository = transactionRepository;
        this.accountServiceJpa = accountServiceJpa;
    }

    @Override
    @Transactional
    public Optional<Transaction> createTransaction(Transaction transaction) throws Exception {

        isValidTransaction(transaction);
        transactionRepository.findById(transaction.getId()).ifPresent(existingTransaction -> {
            throw new IllegalArgumentException("Transaction already exists");
        });

     
        Account sendingAccount = transaction.getFromAccount();
        Account receivingAccount = transaction.getToAccount();

        // validate if accounts are from our bank
        if (!IsInternalTransaction(sendingAccount, receivingAccount)) {
            throw new Exception("this transaction is not whit accounts from our bank");
        }

        // check if the transaction is to the same account
        if (sendingAccount.getIban().equals(receivingAccount.getIban())) {
            throw new IllegalArgumentException("cant transfer to the same account");
        }

        // check if the transaction is to another persons account
        if (!receivingAccount.getUser().equals(sendingAccount.getUser())) {
            // check if the transaction is to another persons savings account
            if (receivingAccount.getAccountType() == AccountType.SAVINGS
                    || sendingAccount.getAccountType() == AccountType.SAVINGS) {
                throw new IllegalArgumentException("cant transfer money to or from another persons saving account");
            }

        }
        // check if account reached the absolute limit
        reachedAbsoluteLimit(sendingAccount, transaction.getAmount());

        // check if the transaction is below then the daily limit
        reachedDailyTransferLimit(sendingAccount.getUser(), transaction.getAmount(), LocalDateTime.now());
        // check if the transaction is bigger then the transfer limit
        transferAmountBiggerThenTransferLimit(sendingAccount, transaction.getAmount());

        // update account balance
        sendingAccount.setBalance(sendingAccount.getBalance().subtract(transaction.getAmount()));
        receivingAccount.setBalance(receivingAccount.getBalance().add(transaction.getAmount()));

        // save the transaction
        accountServiceJpa.createAccount(sendingAccount);
        accountServiceJpa.createAccount(receivingAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return Optional.of(savedTransaction);

    }

    // check if the transaction is whit local accounts
    public Boolean IsInternalTransaction(Account sendingAccount, Account retrievingAccount) {
        if (sendingAccount == null || retrievingAccount == null) {
            throw new IllegalArgumentException("account is null");
        }
        String sendingAccountIBAN = sendingAccount.getIban().toString();
        String retrievingAccountIBAN = retrievingAccount.getIban().toString();
        return sendingAccount.getIban().getBankCode().equals(retrievingAccount.getIban().getBankCode());
    }

    // check the total amount spend by an user
    // TODO: should transactions done by the admin be included?
    public BigDecimal getUserTotalAmountSpendAtDate(User user, LocalDateTime date) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("user is null");
        }

        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
        List<Transaction> transactionsForUser = transactionRepository
                .findByInitiatorIdOrderByCreatedAtAsc(user.getId()); // change this to a service method if we ever get
                                                                     // one that retrieves all transactions for a single
                                                                     // user
        List<Transaction> filteredTransactions = transactionsForUser.stream()
                .filter(t -> t.getCreatedAt().toLocalDate().equals(date.toLocalDate()))
                .filter(t -> t.getFromAccount().getUser().getId().equals(user.getId()))
                .toList();

        return filteredTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("invalid id given");
        }

        return transactionRepository.findById(id);
    }

    @Override
    public Page<Transaction> getTransactionsByFilter(TransactionFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("No filter provided");
        }

        Pageable pageable = filter.toPageAble();
      
        Specification<Transaction> spec = MakeTransactionsSpecification(filter);
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        return transactionPage;
    }

    @Override
    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("No filter provided");
        }
        return transactionRepository.findOne(MakeTransactionsSpecification(filter));
    }

    @Override
    public Page<Transaction> getAllTransactions(BaseFilter filter) {
        Pageable pageable = filter.toPageAble();
       
        Page<Transaction> accountPage = transactionRepository.findAll(pageable);


        return accountPage;

    }

    @Override
    @Transactional
    public Optional<Transaction> updateTransaction(Long id, Transaction transaction) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("invalid id given");
        }
        if (transaction == null) {
            throw new IllegalArgumentException("transaction is null");
        }
        if (transaction.getId() == null || transaction.getId() <= 0) {
            throw new IllegalArgumentException("transaction id is null");
        }

        transaction.setId(id);
        Transaction existingTransaction = transactionRepository.save(transaction);

        Optional<Transaction> updatedTransaction = getTransactionById(id);

        return updatedTransaction;
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

        // TODO: should i validate the description field?
        return true;
    }

    @Override
    public TransactionDTOResponse toDTO(Transaction model) {
        TransactionDTOResponse transactionDTO = new TransactionDTOResponse();
        transactionDTO.setAmount(model.getAmount());
        transactionDTO.setCreatedAt(model.getCreatedAt());
        transactionDTO.setInitiator(model.getInitiator().getId());
        transactionDTO.setSender(model.getFromAccount().getIban().toString());
        transactionDTO.setReceiver(model.getToAccount().getIban().toString());
        transactionDTO.setDescription(model.getDescription());
        return transactionDTO;
    }

    @Override
    public List<TransactionDTOResponse> toDTO(List<Transaction> transactions) {
        List<TransactionDTOResponse> transactionDTOs = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionDTOs.add(toDTO(transaction));
        }
        return transactionDTOs;
    }

    // TODO: maybe move this to an helper class
    public Boolean reachedAbsoluteLimit(Account account, BigDecimal amountToSubtract) throws Exception {
        BigDecimal resultingAmount = account.getBalance().subtract(amountToSubtract);

        if (resultingAmount.compareTo(account.getAbsoluteLimit()) < 0) {
            throw new Exception("cant spend more then the absolute limit");
        }

        return true;
    }

    public Boolean reachedDailyTransferLimit(User user, BigDecimal amount, LocalDateTime today) throws Exception {

        BigDecimal totalSpendBeforeToday = getUserTotalAmountSpendAtDate(user, today); // total
                                                                                       // amount of
                                                                                       // money spend
        // today
        BigDecimal totalSpendToday = totalSpendBeforeToday.add(amount);
        BigDecimal dailyLimit = user.getDailyLimit(); // users daily limit

        if (totalSpendToday.compareTo(dailyLimit) > 0 || totalSpendToday.equals(dailyLimit)) {
            throw new Exception("daily limit reached"); //

        }

        return true;
    }

    public Boolean transferAmountBiggerThenTransferLimit(Account account, BigDecimal amount) throws Exception {
        if (account.getTransactionLimit().compareTo(amount) < 0) {
            throw new Exception("this amount is more than your transfer limit of the account");
        }
        return true;
    }

}
