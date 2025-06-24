package com.krachbank.api.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;





class UserTest {

    private User user;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setPhoneNumber("1234567890");
        user.setBSN("123456789");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDailyLimit(new BigDecimal("1000.00"));
        user.setTransferLimit(new BigDecimal("500.00"));
        user.setCreatedAt(now);
        user.setVerified(true); 
        user.setActive(true);
        user.setAdmin(false); 
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertEquals("123456789", user.getBSN());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(new BigDecimal("1000.00"), user.getDailyLimit());
        assertEquals(new BigDecimal("500.00"), user.getTransferLimit());
        assertEquals(now, user.getCreatedAt());
        assertTrue(user.isVerified());
        assertTrue(user.isActive());
        assertFalse(user.isAdmin());
    }

    @Test
    void testSetters() {
        user.setId(2L);
        user.setUsername("anotheruser");
        user.setPassword("newpass");
        user.setEmail("another@example.com");
        user.setPhoneNumber("0987654321");
        user.setBSN("987654321");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setDailyLimit(new BigDecimal("2000.00"));
        user.setTransferLimit(new BigDecimal("1000.00"));
        LocalDateTime later = now.plusDays(1);
        user.setCreatedAt(later);
        user.setVerified(false);
        user.setActive(false);
        user.setAdmin(true);

        assertEquals(2L, user.getId());
        assertEquals("anotheruser", user.getUsername());
        assertEquals("newpass", user.getPassword());
        assertEquals("another@example.com", user.getEmail());
        assertEquals("0987654321", user.getPhoneNumber());
        assertEquals("987654321", user.getBSN());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals(new BigDecimal("2000.00"), user.getDailyLimit());
        assertEquals(new BigDecimal("1000.00"), user.getTransferLimit());
        assertEquals(later, user.getCreatedAt());
        assertFalse(user.isVerified());
        assertFalse(user.isActive());
        assertTrue(user.isAdmin());
    }

    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabledWhenActive() {
        user.setActive(true);
        assertTrue(user.isEnabled());
    }

    @Test
    void testIsEnabledWhenInactive() {
        user.setActive(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
    }

    @Test
    void testAllArgsConstructor() {
        User newUser = new User();
        newUser.setId(10L);
        newUser.setUsername("u");
        newUser.setPassword("p");
        newUser.setEmail("e");
        newUser.setPhoneNumber("ph");
        newUser.setBSN("1");
        newUser.setFirstName("f");
        newUser.setLastName("l");
        newUser.setDailyLimit(BigDecimal.ONE);
        newUser.setTransferLimit(BigDecimal.TEN);
        newUser.setCreatedAt(now);
        newUser.setVerified(false);
        newUser.setActive(false);
        newUser.setAdmin(true);
        assertEquals("u", newUser.getUsername());
        assertEquals("p", newUser.getPassword());
        assertEquals("e", newUser.getEmail());
        assertEquals("ph", newUser.getPhoneNumber());
        assertEquals("1", newUser.getBSN());
        assertEquals("f", newUser.getFirstName());
        assertEquals("l", newUser.getLastName());
        assertEquals(BigDecimal.ONE, newUser.getDailyLimit());
        assertEquals(BigDecimal.TEN, newUser.getTransferLimit());
        assertEquals(now, newUser.getCreatedAt());
        assertFalse(newUser.isVerified());
        assertFalse(newUser.isActive());
        assertTrue(newUser.isAdmin());
    }
}