package com.krachbank.api.service;

import com.krachbank.api.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String secretKey = Base64.getEncoder().encodeToString("testsecretkeytestsecretkeytestsecretkey".getBytes());
    private long jwtExpiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
    }

    // Test extractClaim method
    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)),
                        SignatureAlgorithm.HS256)
                .compact();

        String extractedUsername = jwtService.extractEmail(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractUsername_shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertThrows(Exception.class, () -> jwtService.extractEmail(invalidToken));
    }

    // Test generateToken method
    @Test
    void generateToken_shouldReturnValidToken() {
        // Arrange
        String username = "testuser";
        User user = mock(User.class);
        when(user.getUsername()).thenReturn(username);

        // Act
        String token = jwtService.generateToken(user);

        // Assert
        assertNotNull(token);
        String extractedUsername = jwtService.extractEmail(token);
        assertEquals(username, extractedUsername);
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void generateToken_shouldContainCorrectExpiration() {
        // Arrange
        String username = "testuser";
        User user = mock(User.class);
        when(user.getUsername()).thenReturn(username);

        // Act
        String token = jwtService.generateToken(user);

        // Assert
        Date expiration = ReflectionTestUtils.invokeMethod(jwtService, "extractExpiration", token);
        assertNotNull(expiration);
        long now = System.currentTimeMillis();
        assertTrue(expiration.getTime() > now);
        assertTrue(expiration.getTime() <= now + jwtExpiration);
    }

    // Test isTokenValid method
    @Test
    void isTokenValid_shouldReturnTrueForValidTokenAndMatchingUser() {
        String username = "validuser";
        User user = mock(User.class);
        when(user.getUsername()).thenReturn(username);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_shouldReturnFalseForTokenWithDifferentEmail() {
        String email = "user1@example.com";
        String otherEmail = "user2@example.com";

        User userMock = mock(User.class);
        when(userMock.getEmail()).thenReturn(otherEmail);

        // Create a user for token generation with email = user1@example.com
        User tokenUser = new User();
        tokenUser.setEmail(email);

        // Generate token with email as subject
        String token = jwtService.generateToken(tokenUser);

        // Assert token is invalid when checked against a user with a different email
        assertFalse(jwtService.isTokenValid(token, userMock));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        String username = "expireduser";
        User user = mock(User.class);
        when(user.getUsername()).thenReturn(username);

        // Set a very short expiration for this test
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 ms
        String token = jwtService.generateToken(user);

        // Wait to ensure the token is expired
        Thread.sleep(5);

        assertFalse(jwtService.isTokenValid(token, user));

        // Restore expiration for other tests
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
    }

    @Test
    void isTokenValid_shouldThrowExceptionForMalformedToken() {
        String malformedToken = "malformed.token.value";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("anyuser");

        assertThrows(Exception.class, () -> jwtService.isTokenValid(malformedToken, userDetails));
    }

    // Test extractClaim method with custom claim
    @Test
    void extractClaim_shouldReturnCustomClaim() {
        String username = "claimuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "admin");
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)),
                        SignatureAlgorithm.HS256)
                .compact();

        String role = jwtService.extractClaim(token, c -> c.get("role", String.class));
        assertEquals("admin", role);
    }

    // Test generateToken with extra claims
    @Test
    void generateToken_withExtraClaims_shouldContainClaims() {
        String username = "extraclaimsuser";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("department", "finance");
        User user = mock(User.class);
        when(user.getUsername()).thenReturn(username);

        String token = jwtService.generateToken(extraClaims, user);

        String department = jwtService.extractClaim(token, c -> c.get("department", String.class));
        assertEquals("finance", department);
        assertEquals(username, jwtService.extractEmail(token));
    }

    // Test isTokenExpired method
    @Test
    void isTokenExpired_shouldReturnTrueForExpiredToken() throws InterruptedException {
        String username = "expired";
        User user = mock(User.class);
        when(user.getUsername()).thenReturn(username);

        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 ms
        String token = jwtService.generateToken(user);
        Thread.sleep(5);

        assertTrue(ReflectionTestUtils.invokeMethod(jwtService, "isTokenExpired", token));
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
    }

    // Test extractAllClaims method
    @Test
    void extractAllClaims_shouldReturnClaims() {
        String username = "claimsuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("foo", "bar");
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)),
                        SignatureAlgorithm.HS256)
                .compact();

        Claims extractedClaims = ReflectionTestUtils.invokeMethod(jwtService, "extractAllClaims", token);
        assertEquals("bar", extractedClaims.get("foo", String.class));
        assertEquals(username, extractedClaims.getSubject());
    }

    // Test getSignInKey method
    @Test
    void getSignInKey_shouldReturnValidKey() {
        java.security.Key key = ReflectionTestUtils.invokeMethod(jwtService, "getSignInKey");
        assertNotNull(key);
        assertEquals("HmacSHA256", key.getAlgorithm());
    }



}