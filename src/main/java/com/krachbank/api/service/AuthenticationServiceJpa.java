// src/main/java/com/krachbank/api/service/AuthenticationServiceJpa.java
package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager; // For login
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // For login
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krachbank.api.dto.AuthenticationDTO;
import com.krachbank.api.dto.AuthenticationResultDTO;
import com.krachbank.api.dto.LoginRequest;
import com.krachbank.api.dto.RegisterRequest;
import com.krachbank.api.exceptions.InvalidCredentialsException;
import com.krachbank.api.exceptions.UserAlreadyExistsException;
import com.krachbank.api.models.User;
import com.krachbank.api.repository.AuthenticationRepository;

@Service
public class AuthenticationServiceJpa implements AuthenticationService {

    private final AuthenticationRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Injected JwtService
    private final AuthenticationManager authenticationManager; // Injected AuthenticationManager

    public AuthenticationServiceJpa(
            AuthenticationRepository authenticationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) { // Inject AuthenticationManager
        this.authenticationRepository = authenticationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthenticationResultDTO register(RegisterRequest registerRequest) throws UserAlreadyExistsException {
        if (authenticationRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with username '" + registerRequest.getUsername() + "' already exists.");
        }
        if (authenticationRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email '" + registerRequest.getEmail() + "' already exists.");
        }

        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPhoneNumber(registerRequest.getPhoneNumber());
        newUser.setBSN(registerRequest.getBSN());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setVerified(false);
        newUser.setActive(true);
        newUser.setDailyLimit(BigDecimal.valueOf(0.0));
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        User savedUser = authenticationRepository.save(newUser);

        // Generate JWT token for the newly registered user
        String jwtToken = jwtService.generateToken((UserDetails) savedUser); // Use savedUser directly as it implements UserDetails

        AuthenticationDTO authenticatedUserDetails = AuthenticationDTO.fromModel(savedUser);
        return new AuthenticationResultDTO(jwtToken, authenticatedUserDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResultDTO login(LoginRequest loginRequest) throws InvalidCredentialsException {
        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) { // Catch AuthenticationException (or more specific ones)
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        // If authentication succeeds, retrieve the user to generate a token
        User user = authenticationRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found after successful authentication (should not happen)."));

        // Generate JWT token for the authenticated user
        String jwtToken = jwtService.generateToken((UserDetails) user); // Use user directly as it implements UserDetails

        AuthenticationDTO authenticatedUserDetails = AuthenticationDTO.fromModel(user);
        return new AuthenticationResultDTO(jwtToken, authenticatedUserDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return authenticationRepository.findByUsername(username);
    }
}