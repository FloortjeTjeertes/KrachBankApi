package com.krachbank.api.repository;

import com.krachbank.api.models.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager; // Useful for clearing the context or flushing

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Clear the persistence context and database before each test
        userRepository.deleteAll(); // Clears all data from the H2 in-memory DB
        entityManager.flush(); // Ensure changes are written
        entityManager.clear(); // Clear the persistence context

        user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@example.com");
        user1.setPhoneNumber("111-222-3333");
        user1.setBSN(123456789);
        user1.setUsername("john.doe");
        user1.setPassword("encodedPassword1");
        user1.setActive(true);
        user1.setVerified(true);
        user1.setDailyLimit(BigDecimal.valueOf(1000.00));
        user1.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));

        user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setPhoneNumber("444-555-6666");
        user2.setBSN(987654321);
        user2.setUsername("jane.smith");
        user2.setPassword("encodedPassword2");
        user2.setActive(true);
        user2.setVerified(false);
        user2.setDailyLimit(BigDecimal.valueOf(500.00));
        user2.setCreatedAt(LocalDateTime.of(2024, 2, 15, 12, 30));

        user3 = new User();
        user3.setFirstName("Peter");
        user3.setLastName("Jones");
        user3.setEmail("peter.jones@example.com");
        user3.setPhoneNumber("777-888-9999");
        user3.setBSN(112233445);
        user3.setUsername("peter.jones");
        user3.setPassword("encodedPassword3");
        user3.setActive(false); // Inactive user
        user3.setVerified(true);
        user3.setDailyLimit(BigDecimal.valueOf(2000.00));
        user3.setCreatedAt(LocalDateTime.of(2023, 5, 20, 8, 0));

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        entityManager.flush(); // Ensure entities are written to the database
        entityManager.clear(); // Detach entities from the persistence context
    }

    @Test
    @DisplayName("Should find a user by ID")
    void findById_ShouldReturnUser() {
        Optional<User> foundUserOptional = userRepository.findById(user1.getId());
        assertThat(foundUserOptional).isPresent();
        assertThat(foundUserOptional.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return empty optional if user not found by ID")
    void findById_ShouldReturnEmptyOptional_WhenNotFound() {
        Optional<User> foundUserOptional = userRepository.findById(999L); // Non-existent ID
        assertThat(foundUserOptional).isNotPresent();
    }

    @Test
    @DisplayName("Should find a user by email")
    void findByEmail_ShouldReturnUser() {
        Optional<User> foundUserOptional = userRepository.findByEmail("john.doe@example.com");
        assertThat(foundUserOptional).isPresent();
        assertThat(foundUserOptional.get().getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should return empty optional if user not found by email")
    void findByEmail_ShouldReturnEmptyOptional_WhenNotFound() {
        Optional<User> foundUserOptional = userRepository.findByEmail("nonexistent@example.com");
        assertThat(foundUserOptional).isNotPresent();
    }

    @Test
    @DisplayName("Should find users by first name")
    void findByFirstName_ShouldReturnUsers() {
        List<User> users = userRepository.findByFirstName("John");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return empty list if no users found by first name")
    void findByFirstName_ShouldReturnEmptyList_WhenNotFound() {
        List<User> users = userRepository.findByFirstName("NonExistent");
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should find users by last name")
    void findByLastName_ShouldReturnUsers() {
        List<User> users = userRepository.findByLastName("Smith");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should return empty list if no users found by last name")
    void findByLastName_ShouldReturnEmptyList_WhenNotFound() {
        List<User> users = userRepository.findByLastName("NonExistent");
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should find a user by username")
    void findByUsername_ShouldReturnUser() {
        Optional<User> foundUserOptional = userRepository.findByUsername("jane.smith");
        assertThat(foundUserOptional).isPresent();
        assertThat(foundUserOptional.get().getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should return empty optional if user not found by username")
    void findByUsername_ShouldReturnEmptyOptional_WhenNotFound() {
        Optional<User> foundUserOptional = userRepository.findByUsername("nonexistent.user");
        assertThat(foundUserOptional).isNotPresent();
    }

    @Test
    @DisplayName("Should find users by phone number")
    void findByPhoneNumber_ShouldReturnUsers() {
        List<User> users = userRepository.findByPhoneNumber("111-222-3333");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should return empty list if no users found by phone number")
    void findByPhoneNumber_ShouldReturnEmptyList_WhenNotFound() {
        List<User> users = userRepository.findByPhoneNumber("000-000-0000");
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should find active users")
    void findByActive_ShouldReturnActiveUsers() {
        List<User> activeUsers = userRepository.findByActive(true);
        assertThat(activeUsers).hasSize(2); // user1, user2 are active
        assertThat(activeUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.smith@example.com");
    }

    @Test
    @DisplayName("Should find inactive users")
    void findByActive_ShouldReturnInactiveUsers() {
        List<User> inactiveUsers = userRepository.findByActive(false);
        assertThat(inactiveUsers).hasSize(1); // user3 is inactive
        assertThat(inactiveUsers.get(0).getEmail()).isEqualTo("peter.jones@example.com");
    }

    @Test
    @DisplayName("Should find verified users")
    void findByVerified_ShouldReturnVerifiedUsers() {
        List<User> verifiedUsers = userRepository.findByVerified(true);
        assertThat(verifiedUsers).hasSize(2); // user1, user3 are verified
        assertThat(verifiedUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "peter.jones@example.com");
    }

    @Test
    @DisplayName("Should find unverified users")
    void findByVerified_ShouldReturnUnverifiedUsers() {
        List<User> unverifiedUsers = userRepository.findByVerified(false);
        assertThat(unverifiedUsers).hasSize(1); // user2 is unverified
        assertThat(unverifiedUsers.get(0).getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should find all users with JpaRepository.findAll()")
    void findAll_ShouldReturnAllUsers() {
        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(3);
    }

    @Test
    @DisplayName("Should save a new user")
    void save_ShouldPersistNewUser() {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new.user@example.com");
        newUser.setPhoneNumber("999-888-7777");
        newUser.setBSN(100100100);
        newUser.setUsername("new.user");
        newUser.setPassword("newEncodedPassword");
        newUser.setActive(true);
        newUser.setVerified(false);
        newUser.setDailyLimit(BigDecimal.valueOf(1500.00));
        newUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);
        entityManager.flush(); // Ensure it's written to DB
        entityManager.clear(); // Clear cache to fetch fresh from DB

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull(); // ID should be generated
        assertThat(userRepository.findById(savedUser.getId())).isPresent();
        assertThat(userRepository.findById(savedUser.getId()).get().getEmail()).isEqualTo("new.user@example.com");
    }

    @Test
    @DisplayName("Should update an existing user")
    void save_ShouldUpdateExistingUser() {
        // Retrieve a user, modify it, and save
        Optional<User> retrievedUserOptional = userRepository.findById(user1.getId());
        assertThat(retrievedUserOptional).isPresent();
        User userToUpdate = retrievedUserOptional.get();

        userToUpdate.setFirstName("Jonathan");
        userToUpdate.setEmail("jonathan.doe@example.com");

        User updatedUser = userRepository.save(userToUpdate);
        entityManager.flush();
        entityManager.clear();

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(user1.getId());
        assertThat(userRepository.findById(user1.getId()).get().getFirstName()).isEqualTo("Jonathan");
        assertThat(userRepository.findById(user1.getId()).get().getEmail()).isEqualTo("jonathan.doe@example.com");
    }

    @Test
    @DisplayName("Should delete a user by ID")
    void deleteById_ShouldRemoveUser() {
        userRepository.deleteById(user1.getId());
        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.findById(user1.getId())).isNotPresent();
        assertThat(userRepository.findAll()).hasSize(2);
    }

    // Testing JpaSpecificationExecutor methods
    @Test
    @DisplayName("Should find users using JpaSpecificationExecutor by email")
    void findAll_WithSpecification_ShouldFilterByEmail() {
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("email"), "john.doe@example.com");
        List<User> users = userRepository.findAll(spec);
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should find users using JpaSpecificationExecutor by active status")
    void findAll_WithSpecification_ShouldFilterByActiveStatus() {
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("active"), false);
        List<User> users = userRepository.findAll(spec);
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("peter.jones@example.com");
    }

    @Test
    @DisplayName("Should find users using JpaSpecificationExecutor by date created after")
    void findAll_WithSpecification_ShouldFilterByCreatedAtAfter() {
        LocalDateTime cutoff = LocalDateTime.of(2024, 1, 1, 0, 0); // Users created after 2024-01-01
        Specification<User> spec = (root, query, cb) -> cb.greaterThan(root.get("createdAt"), cutoff);
        List<User> users = userRepository.findAll(spec);
        assertThat(users).hasSize(1); // Only user2 (2024-02-15)
        assertThat(users.get(0).getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should find users using JpaSpecificationExecutor with pagination")
    void findAll_WithSpecificationAndPageable_ShouldReturnPagedResults() {
        Specification<User> spec = (root, query, cb) -> cb.isTrue(root.get("active"));
        Pageable pageable = PageRequest.of(0, 1); // Get 1 user per page, first page

        Page<User> usersPage = userRepository.findAll(spec, pageable);

        assertThat(usersPage).isNotNull();
        assertThat(usersPage.getTotalElements()).isEqualTo(2); // user1, user2 are active
        assertThat(usersPage.getTotalPages()).isEqualTo(2);
        assertThat(usersPage.getContent()).hasSize(1);
        // The order might vary, so we can't assert the exact user unless sorted explicitly
        // For simplicity, we just check content size
    }
}