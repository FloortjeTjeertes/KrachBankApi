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
import com.krachbank.api.filters.TransactionFilter;
import com.krachbank.api.models.Account;
import com.krachbank.api.models.AccountType;
import com.krachbank.api.models.Transaction;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.TransactionRepository;
import com.krachbank.api.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

@Service
public class TransactionServiceJpa implements TransactionService {
    private static final Long ATM_USER_ID = 3L; // Add this line - assuming ATM's owner ID is 3

    private final AccountServiceJpa accountServiceJpa;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    public TransactionServiceJpa(TransactionRepository transactionRepository, AccountServiceJpa accountServiceJpa,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountServiceJpa = accountServiceJpa;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Optional<Transaction> createTransaction(Transaction transaction, String userName) throws Exception {

        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        transaction.setInitiator(user.get());

        // Validate the transaction
        isValidTransaction(transaction);

        Account sendingAccount = transaction.getFromAccount();
        Account receivingAccount = transaction.getToAccount();

        // validate if accounts are from our bank
        IsInternalTransaction(sendingAccount, receivingAccount);

        // Check if the transaction is to the same account
        if (sendingAccount.getIban().equals(receivingAccount.getIban())) {
            throw new IllegalArgumentException("Can't transfer to the same account.");
        }

        // TODO: check if the user is an admin - This is still relevant.

        if (!sendingAccount.getUser().getId().equals(ATM_USER_ID) && !sendingAccount.getUser().equals(user.get())) {
            throw new IllegalArgumentException(
                    "You can only transfer money from your own accounts or the ATM account.");
        }

        // check if the transaction is to another persons account
        if (!receivingAccount.getUser().equals(sendingAccount.getUser())) {
            if (receivingAccount.getAccountType() == AccountType.SAVINGS
                    || sendingAccount.getAccountType() == AccountType.SAVINGS) {
                throw new IllegalArgumentException("Can't transfer money to or from another person's saving account");
            }

        }
        // check if account reached the absolute limit (for sender)
        reachedAbsoluteLimit(sendingAccount, transaction.getAmount());

        // check if the transaction is below the daily limit (for initiator/user)
        reachedDailyTransferLimit(user.get(), transaction.getAmount(), LocalDateTime.now());

        // check if the transaction is bigger than the transfer limit (for sender)
        transferAmountBiggerThenTransferLimit(sendingAccount, transaction.getAmount());

        // update account balance
        sendingAccount.setBalance(sendingAccount.getBalance().subtract(transaction.getAmount()));
        receivingAccount.setBalance(receivingAccount.getBalance().add(transaction.getAmount()));

        // save the transaction
        accountServiceJpa.createAccount(sendingAccount); // Assuming createAccount also handles updates
        accountServiceJpa.createAccount(receivingAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return Optional.of(savedTransaction);
    }

    // check if the transaction is whit local accounts
    public void IsInternalTransaction(Account sendingAccount, Account retrievingAccount) {
        if (sendingAccount == null || retrievingAccount == null) {
            throw new IllegalArgumentException("account is null");
        }
        String sendingAccountBankCode = sendingAccount.getIban().getBankCode();
        String retrievingAccountBankCode = retrievingAccount.getIban().getBankCode();
        if (!sendingAccountBankCode.equals(retrievingAccountBankCode)) {
            throw new IllegalArgumentException("The accounts are not from the same bank");
        }
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
        transactionRepository.save(transaction);

        Optional<Transaction> updatedTransaction = getTransactionById(id);

        return updatedTransaction;
    }

    @Override
    public Page<Transaction> getUserTransactions(Long userId, TransactionFilter filter) {
        // Get all transactions where the user is either the sender or receiver
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("invalid user id given");
        }
        if (filter == null) {
            filter = new TransactionFilter();
        }

        Specification<Transaction> filterSpecification = toAndOrFromAccountBelongsToUser(userId);

        // If the filter has other criteria, combine them with the user ID filter (note:
        // combining whit null value has no negative effect)
        if (filter != null) {
            filterSpecification = filterSpecification.and(MakeTransactionsSpecification(filter));
        }

        Page<Transaction> allTransactions = transactionRepository
                .findAll(filterSpecification, filter.toPageAble());

        return allTransactions;
    }

    @Override
    public Page<Transaction> getTransactionsByIBAN(String iban, TransactionFilter filter) {
        if (iban == null || iban.isEmpty()) {
            throw new IllegalArgumentException("invalid iban given");
        }
        if (filter == null) {
            filter = new TransactionFilter();
        }
        Specification<Transaction> spec = MakeTransactionsSpecification(filter);
        spec = spec.and(toAndOrFromAccount(iban));

        Page<Transaction> transactionPage = transactionRepository.findAll(spec, filter.toPageAble());

        return transactionPage;
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

    public static Specification<Transaction> MakeTransactionsSpecification(TransactionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSenderIban() != null) {
                predicates.add(cb.equal(root.get("fromAccount").get("iban"), filter.getSenderIban()));
            }
            if (filter.getReceiverIban() != null) {
                predicates.add(cb.equal(root.get("toAccount").get("iban"), filter.getReceiverIban()));
            }
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            if (filter.getBeforeDate() != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), filter.getBeforeDate()));
            }
            if (filter.getAfterDate() != null) {
                predicates.add(cb.greaterThan(root.get("createdAt"), filter.getAfterDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

    }

    public static Specification<Transaction> toAndOrFromAccountBelongsToUser(Long userID) {

        if (userID == null || userID <= 0) {
            throw new IllegalArgumentException("Account ID cannot be null or negative");
        }
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("fromAccount").get("user").get("id"), userID),
                cb.equal(root.get("toAccount").get("user").get("id"), userID));
    }

    public static Specification<Transaction> toAndOrFromAccount(String iban) {
        if (iban == null || iban.isEmpty()) {
            throw new IllegalArgumentException("iban cannot be null or empty");
        }
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("fromAccount").get("iban"), iban),
                cb.equal(root.get("toAccount").get("iban"), iban));
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

    // remove this method if not needed
    @Override
    public TransactionDTOResponse toDTO(Transaction model) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toDTO'");
    }

    @Override
    public List<TransactionDTOResponse> toDTO(List<Transaction> fields) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toDTO'");
    }

}
