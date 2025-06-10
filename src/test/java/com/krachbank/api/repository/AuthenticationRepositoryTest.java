package com.krachbank.api.repository;

import com.krachbank.api.models.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Configures an in-memory database and JPA components for testing
class AuthenticationRepositoryTest {

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private EntityManager entityManager; // To manage persistence context (flush/clear)

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Clear all data before each test to ensure test isolation
        authenticationRepository.deleteAll();
        entityManager.flush(); // Ensure deletion is committed
        entityManager.clear(); // Clear JPA persistence context

        user1 = new User();
        user1.setFirstName("Alice");
        user1.setLastName("Smith");
        user1.setUsername("alice_smith");
        user1.setEmail("alice.smith@example.com");
        user1.setPhoneNumber("111-111-1111");
        user1.setBSN(111222333);
        user1.setPassword("hashedpass1"); // Password field is not used in findByUsername/Email
        user1.setCreatedAt(LocalDateTime.now());
        user1.setVerified(true);
        user1.setActive(true);
        user1.setDailyLimit(BigDecimal.valueOf(1000.00));

        user2 = new User();
        user2.setFirstName("Bob");
        user2.setLastName("Johnson");
        user2.setUsername("bob_johnson");
        user2.setEmail("bob.johnson@example.com");
        user2.setPhoneNumber("222-222-2222");
        user2.setBSN(444555666);
        user2.setPassword("hashedpass2");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setVerified(false);
        user2.setActive(true);
        user2.setDailyLimit(BigDecimal.valueOf(500.00));

        // Save users to the in-memory database
        authenticationRepository.save(user1);
        authenticationRepository.save(user2);

        entityManager.flush(); // Commit changes to the database
        entityManager.clear(); // Detach entities from the persistence context to ensure fresh reads
    }

    @Test
    @DisplayName("findByUsername - Should find a user by their username")
    void findByUsername_ShouldReturnUserWhenFound() {
        // Act
        Optional<User> foundUser = authenticationRepository.findByUsername("alice_smith");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("alice_smith");
        assertThat(foundUser.get().getEmail()).isEqualTo("alice.smith@example.com");
    }

    @Test
    @DisplayName("findByUsername - Should return empty optional when username not found")
    void findByUsername_ShouldReturnEmptyOptionalWhenNotFound() {
        // Act
        Optional<User> foundUser = authenticationRepository.findByUsername("nonexistent_user");

        // Assert
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("findByEmail - Should find a user by their email")
    void findByEmail_ShouldReturnUserWhenFound() {
        // Act
        Optional<User> foundUser = authenticationRepository.findByEmail("bob.johnson@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("bob.johnson@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("bob_johnson");
    }

    @Test
    @DisplayName("findByEmail - Should return empty optional when email not found")
    void findByEmail_ShouldReturnEmptyOptionalWhenNotFound() {
        // Act
        Optional<User> foundUser = authenticationRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("findById - Should find a user by their ID (inherited from JpaRepository)")
    void findById_ShouldReturnUserWhenFound() {
        // Get the ID from the saved user1
        Long userId = user1.getId();
        assertThat(userId).isNotNull(); // Ensure ID was generated

        // Act
        Optional<User> foundUser = authenticationRepository.findById(userId);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getUsername()).isEqualTo("alice_smith");
    }

    @Test
    @DisplayName("findById - Should return empty optional when ID not found")
    void findById_ShouldReturnEmptyOptionalWhenNotFound() {
        // Act
        Optional<User> foundUser = authenticationRepository.findById(999L); // A non-existent ID

        // Assert
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("save - Should persist a new user")
    void save_ShouldPersistNewUser() {
        // Arrange
        User newUser = new User();
        newUser.setFirstName("Charlie");
        newUser.setLastName("Brown");
        newUser.setUsername("charlie.brown");
        newUser.setEmail("charlie.brown@example.com");
        newUser.setPhoneNumber("333-333-3333");
        newUser.setBSN(777888999);
        newUser.setPassword("newhashedpass");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setVerified(false);
        newUser.setActive(true);
        newUser.setDailyLimit(BigDecimal.valueOf(200.00));

        // Act
        User savedUser = authenticationRepository.save(newUser);
        entityManager.flush(); // Ensure written to DB
        entityManager.clear(); // Clear cache

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull(); // ID should be generated
        Optional<User> found = authenticationRepository.findById(savedUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("charlie.brown");
    }

    @Test
    @DisplayName("delete - Should remove a user")
    void delete_ShouldRemoveUser() {
        // Arrange
        Long userIdToDelete = user1.getId();

        // Act
        authenticationRepository.deleteById(userIdToDelete);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<User> found = authenticationRepository.findById(userIdToDelete);
        assertThat(found).isNotPresent(); // User should no longer be found
        assertThat(authenticationRepository.findAll()).hasSize(1); // Only user2 should remain
    }
}