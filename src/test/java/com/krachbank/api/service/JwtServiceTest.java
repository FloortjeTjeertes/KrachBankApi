/*
package com.krachbank.api.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field; // Import for reflection
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    // Simulate @Value injection by directly setting fields
    private final String TEST_SECRET_KEY = "5F1J5r9K2L8N4M6P7Q3R5T7W9Y2B4D6F8H0J1K3M5P7S9V1A3C5E7G9I1O3U5W7Z";
    private final long TEST_JWT_EXPIRATION = 1000 * 60 * 60 * 24; // 24 hours in milliseconds

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException { // Add exceptions for reflection
        jwtService = new JwtService();

        // --- Start of Reflection Code ---
        // Get the private 'secretKey' field
        Field secretKeyField = JwtService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true); // Allow access to private field
        secretKeyField.set(jwtService, TEST_SECRET_KEY); // Set its value on the instance

        // Get the private 'jwtExpiration' field
        Field jwtExpirationField = JwtService.class.getDeclaredField("jwtExpiration");
        jwtExpirationField.setAccessible(true); // Allow access to private field
        jwtExpirationField.set(jwtService, TEST_JWT_EXPIRATION); // Set its value on the instance
        // --- End of Reflection Code ---

        // Common mock setup for UserDetails
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    // --- You will NOT need to add setSecretKey and setJwtExpiration to JwtService.java anymore ---
    // public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    // public void setJwtExpiration(long jwtExpiration) { this.jwtExpiration = jwtExpiration; }


    @Test
    @DisplayName("generateToken(UserDetails) - Should generate a valid token with correct subject and claims")
    void generateToken_UserDetails_ShouldGenerateValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertThat(token).isNotEmpty();

        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("testuser");

        Date issuedAt = jwtService.extractClaim(token, c -> c.getIssuedAt());
        Date expiration = jwtService.extractClaim(token, c -> c.getExpiration());

        assertThat(issuedAt).isBefore(new Date());
        assertThat(expiration).isAfter(issuedAt);
        // Ensure expiration is roughly TEST_JWT_EXPIRATION milliseconds from issuedAt
        assertThat(expiration.getTime() - issuedAt.getTime()).isCloseTo(TEST_JWT_EXPIRATION, Percentage.withPercentage(1000L)); // Allow 1 second variance
    }

    @Test
    @DisplayName("generateToken(Map, UserDetails) - Should generate a token with extra claims")
    void generateToken_ExtraClaims_ShouldIncludeExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", 123L);

        String token = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(token);
        String role = jwtService.extractClaim(token, claims -> (String) claims.get("role"));
        Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));

        assertThat(role).isEqualTo("ADMIN");
        assertThat(userId).isEqualTo(123L);
    }

    @Test
    @DisplayName("extractUsername - Should correctly extract username from token")
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    @DisplayName("isTokenValid - Should return true for a valid, non-expired token matching userDetails")
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("isTokenValid - Should return false for an expired token")
    void isTokenValid_ExpiredToken_ReturnsFalse() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Temporarily set a very short expiration for this test using reflection
        Field jwtExpirationField = JwtService.class.getDeclaredField("jwtExpiration");
        jwtExpirationField.setAccessible(true);
        jwtExpirationField.set(jwtService, 100L); // 100 milliseconds
        String token = jwtService.generateToken(userDetails);

        // Wait for the token to expire
        Thread.sleep(150); // Sleep more than expiration time

        assertFalse(jwtService.isTokenValid(token, userDetails));

        // Reset expiration for other tests by setting it back to the original value
        jwtExpirationField.set(jwtService, TEST_JWT_EXPIRATION);
    }

    @Test
    @DisplayName("isTokenValid - Should return false for a token with a different username")
    void isTokenValid_DifferentUsername_ReturnsFalse() {
        String token = jwtService.generateToken(userDetails); // Token for 'testuser'

        // Create another UserDetails with a different username
        UserDetails anotherUserDetails = org.mockito.Mockito.mock(UserDetails.class);
        when(anotherUserDetails.getUsername()).thenReturn("anotheruser");

        assertFalse(jwtService.isTokenValid(token, anotherUserDetails));
    }

    @Test
    @DisplayName("isTokenExpired - Should return true for an expired token")
    void isTokenExpired_ExpiredToken_ReturnsTrue() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Temporarily set a very short expiration for this specific test using reflection
        Field jwtExpirationField = JwtService.class.getDeclaredField("jwtExpiration");
        jwtExpirationField.setAccessible(true);
        jwtExpirationField.set(jwtService, 10L); // 10 milliseconds
        String token = jwtService.generateToken(userDetails);

        // Wait for the token to expire
        Thread.sleep(50); // Sleep more than the expiration time

        assertTrue(jwtService.isTokenExpired(token));

        // Reset expiration for other tests by setting it back to the original value
        jwtExpirationField.set(jwtService, TEST_JWT_EXPIRATION);
    }

    @Test
    @DisplayName("isTokenExpired - Should return false for a non-expired token")
    void isTokenExpired_NonExpiredToken_ReturnsFalse() {
        // Default TEST_JWT_EXPIRATION (24 hours) is long enough for this
        String token = jwtService.generateToken(userDetails);
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("extractClaim - Should extract any claim correctly")
    void extractClaim_ShouldExtractAnyClaim() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "value123");
        String token = jwtService.generateToken(extraClaims, userDetails);

        String customClaimValue = jwtService.extractClaim(token, claims -> (String) claims.get("customClaim"));
        assertThat(customClaimValue).isEqualTo("value123");
    }

    @Test
    @DisplayName("extractAllClaims - Should throw exception for invalid token format")
    void extractAllClaims_InvalidTokenFormat_ThrowsException() {
        String malformedToken = "not.a.real.jwt";
        assertThrows(MalformedJwtException.class, () -> jwtService.extractAllClaims(malformedToken));
    }

    @Test
    @DisplayName("extractAllClaims - Should throw exception for token with invalid signature")
    void extractAllClaims_InvalidSignature_ThrowsException() throws NoSuchFieldException, IllegalAccessException {
        // Generate a token with the correct service
        String validToken = jwtService.generateToken(userDetails);

        // Create a new JwtService instance
        JwtService maliciousJwtService = new JwtService();

        // Use reflection to set a DIFFERENT secret key for this malicious service
        Field maliciousSecretKeyField = JwtService.class.getDeclaredField("secretKey");
        maliciousSecretKeyField.setAccessible(true);
        maliciousSecretKeyField.set(maliciousJwtService, "ANOTHER_SECRET_KEY_FOR_TAMPERING_DONT_USE_THIS_IN_PROD_DANGER_LONG_ENOUGH"); // Different key
        // Also set expiration for completeness, though not strictly needed for SignatureException
        Field maliciousJwtExpirationField = JwtService.class.getDeclaredField("jwtExpiration");
        maliciousJwtExpirationField.setAccessible(true);
        maliciousJwtExpirationField.set(maliciousJwtService, TEST_JWT_EXPIRATION);


        // Try to parse the valid token with the malicious service's key
        assertThrows(SignatureException.class, () -> maliciousJwtService.extractAllClaims(validToken));
    }

    @Test
    @DisplayName("extractAllClaims - Should throw exception for expired token when trying to extract claims directly")
    void extractAllClaims_ExpiredToken_ThrowsExpiredJwtException() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // Temporarily set a very short expiration for this specific test using reflection
        Field jwtExpirationField = JwtService.class.getDeclaredField("jwtExpiration");
        jwtExpirationField.setAccessible(true);
        jwtExpirationField.set(jwtService, 10L); // 10 milliseconds
        String token = jwtService.generateToken(userDetails);

        // Wait for the token to expire
        Thread.sleep(50); // Sleep more than the expiration time

        // Directly extracting claims from an expired token usually throws ExpiredJwtException
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractAllClaims(token));

        // Reset expiration for other tests by setting it back to the original value
        jwtExpirationField.set(jwtService, TEST_JWT_EXPIRATION);
    }
}*/
