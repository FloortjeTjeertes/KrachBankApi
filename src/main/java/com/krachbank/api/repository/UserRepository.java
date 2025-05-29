package com.krachbank.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krachbank.api.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods based on naming conventions:
    List<User> findByEmail(String email);
    List<User> findByFirstName(String firstName);
    List<User> findByLastName(String lastName);
}