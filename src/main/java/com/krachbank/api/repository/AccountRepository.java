package com.krachbank.api.repository;

import java.util.Optional;

import org.iban4j.Iban;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.krachbank.api.models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> , JpaSpecificationExecutor<Account> {
    Optional<Account> findByIban(Iban iban);
    Page<Account> findByUserId(Pageable pageable,Specification<Account> specification);
} 
