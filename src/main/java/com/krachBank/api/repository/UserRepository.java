package com.krachBank.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krachBank.api.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


}
