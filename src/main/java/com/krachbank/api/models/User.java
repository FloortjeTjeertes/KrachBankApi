package com.krachbank.api.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List; // For simplicity, using a fixed list of authorities

import com.krachbank.api.dto.DTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Model, UserDetails { // Implement UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Stores hashed password

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;

    private String BSN;

    private String firstName;

    private String lastName;

    private Double dailyLimit;

    private Double transferLimit;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    // --- UserDetails interface methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For simplicity, returning a fixed authority (e.g., "ROLE_USER").
        // In a real application, you'd fetch roles from the database for this user.
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password; // Returns the hashed password
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // You can implement account expiration logic if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // You can implement account locking logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // You can implement credential expiration logic if needed
    }

    @Override
    public boolean isEnabled() {
        return this.active; // Account is enabled if 'active' is true
    }

    @Override
    public DTO toDTO() {
        return null;
    }

    public String getBsn() {
        try {
            return BSN;
        } catch (NumberFormatException e) {
            // Handle the case where BSN is not a valid integer
            throw new IllegalArgumentException("BSN must be a valid integer", e);
        }
    }

    public String getBSN() {
        if (BSN == null || BSN.isEmpty()) {
            return null; // Return null if BSN is not set
        }
        return BSN; // Return the BSN as a string
    }
}