
package com.krachbank.api.repository;

import com.krachbank.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}