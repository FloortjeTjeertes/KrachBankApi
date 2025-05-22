package com.krachBank.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import com.krachBank.api.filters.TransactionFilter;
import com.krachBank.api.models.Transaction;
import com.krachBank.api.repository.TransactionRepository;

@Service
public class TransactionJpa implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionJpa(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Optional<Transaction> createTransaction(Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTransaction'");
    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionById'");
    }

    @Override
    public List<Transaction> getTransactionsByFilter(TransactionFilter filter) {

        return transactionRepository.findAll(MakeTransactionsSpecification(filter));

    }

    @Override
    public Optional<Transaction> getTransactionByFilter(TransactionFilter filter) {
        // TODO Auto-generated method stub
        return transactionRepository.findOne(MakeTransactionsSpecification(filter));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTransactions'");
    }

    @Override
    public Optional<Transaction> updateTransaction(Long id, Transaction transaction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTransaction'");
    }

    public static Specification<Transaction> MakeTransactionsSpecification(TransactionFilter filter) {
    return  (root,query,cb)->{
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getSenderId() != null) {
            cb.and(cb.equal(root.get("fromAccount").get("id"), filter.getSenderId()));
        }
        if (filter.getReceiverId() != null) {
            cb.and(cb.equal(root.get("toAccount").get("id"), filter.getReceiverId()));
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



}
