package com.krachbank.api.dto;

import com.krachbank.api.models.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationDTOTest {

    @Test
    void testFromModel() {
        User user = new User();
        user.setId(2L);
        user.setUsername("anotheruser");
        user.setEmail("another@example.com");
        user.setPhoneNumber("0987654321");
        user.setBSN("987654321");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setDailyLimit(new BigDecimal("500.00"));
        user.setCreatedAt(LocalDateTime.now());
        user.setVerified(false);
        user.setActive(false);
        user.setAdmin(true);

        AuthenticationDTO dto = AuthenticationDTO.fromModel(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getUsername(), dto.getUsername());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getPhoneNumber(), dto.getPhoneNumber());
        assertEquals(user.getBSN(), dto.getBSN());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getDailyLimit(), dto.getDailyLimit());
        assertEquals(user.getCreatedAt(), dto.getCreatedAt());
        assertEquals(user.isVerified(), dto.isVerified());
        assertEquals(user.isActive(), dto.isActive());
        assertEquals(user.isAdmin(), dto.isAdmin());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AuthenticationDTO dto = new AuthenticationDTO();
        dto.setId(3L);
        dto.setUsername("setuser");
        dto.setEmail("set@example.com");
        dto.setPhoneNumber("1112223333");
        dto.setBSN("111222333");
        dto.setFirstName("Set");
        dto.setLastName("User");
        dto.setDailyLimit(new BigDecimal("200.00"));
        dto.setCreatedAt(LocalDateTime.now());
        dto.setVerified(true);
        dto.setActive(false);
        dto.setAdmin(false);

        assertEquals(3L, dto.getId());
        assertEquals("setuser", dto.getUsername());
        assertEquals("set@example.com", dto.getEmail());
        assertEquals("1112223333", dto.getPhoneNumber());
        assertEquals(111222333, dto.getBSN());
        assertEquals("Set", dto.getFirstName());
        assertEquals("User", dto.getLastName());
        assertEquals(new BigDecimal("200.00"), dto.getDailyLimit());
        assertNotNull(dto.getCreatedAt());
        assertTrue(dto.isVerified());
        assertFalse(dto.isActive());
        assertFalse(dto.isAdmin());
    }
}