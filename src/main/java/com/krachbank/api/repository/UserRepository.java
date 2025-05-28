package com.krachbank.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.krachbank.api.models.User;

import jakarta.validation.Valid;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @GetMapping("/users")
    List<User> findAll();

    @GetMapping("/users/{id}")
    Optional<User> findById(@PathVariable Long id);

    @PostMapping("/users")
    User save(@Valid @RequestBody User user);

    @PutMapping("/users/{id}")
    User update(@PathVariable Long id, @Valid @RequestBody User user);

    @DeleteMapping("/users/{id}")
    void deleteById(@PathVariable Long id);

    List<User> findByEmail(String email);

    List<User> findByFirstName(String firstName);

    List<User> findByLastName(String lastName);

}
