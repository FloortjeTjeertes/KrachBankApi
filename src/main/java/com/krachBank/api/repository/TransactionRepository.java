package com.krachbank.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.krachbank.api.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    // Custom query methods can be defined here if needed
    // For example, you can define a method to find transactions by user ID or date range
    // List<Transaction> findByUserId(Long userId);
    

}
