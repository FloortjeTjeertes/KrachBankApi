package com.krachbank.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krachbank.api.models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    

    
} 
