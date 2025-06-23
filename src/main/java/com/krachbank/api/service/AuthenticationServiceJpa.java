package com.krachbank.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.AuthenticationException; // Import this

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
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceJpa(
            AuthenticationRepository authenticationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.authenticationRepository = authenticationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthenticationResultDTO register(RegisterRequest registerRequest) throws UserAlreadyExistsException {
        if (authenticationRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email '" + registerRequest.getEmail() + "' already exists.");
        }

        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        // If username is derived from first+last name, set it here for the User object
        newUser.setUsername(registerRequest.getFirstName() + registerRequest.getLastName()); // Make sure this matches your UserService logic
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPhoneNumber(registerRequest.getPhoneNumber());
        newUser.setBSN(Integer.parseInt(registerRequest.getBSN()));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setVerified(false);
        newUser.setActive(true);
        newUser.setDailyLimit(BigDecimal.valueOf(0.0));
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        User savedUser = authenticationRepository.save(newUser);

        String jwtToken = jwtService.generateToken((UserDetails) savedUser);

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
                            loginRequest.getEmail(), // Use email as the principal for authentication
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) { // Catch AuthenticationException, which is more specific
            throw new InvalidCredentialsException("Invalid email or password."); // More specific error message
        }

        // If authentication succeeds, retrieve the user to generate a token
        // *** CRITICAL FIX: Use findByEmail here, not findByUsername ***
        User user = authenticationRepository.findByEmail(loginRequest.getEmail()) // <--- CHANGED THIS LINE
                .orElseThrow(() -> new InvalidCredentialsException("User not found after successful authentication (this indicates a deeper issue)."));

        // Generate JWT token for the authenticated user
        String jwtToken = jwtService.generateToken((UserDetails) user);

        AuthenticationDTO authenticatedUserDetails = AuthenticationDTO.fromModel(user);
        return new AuthenticationResultDTO(jwtToken, authenticatedUserDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        // This method still uses findByUsername. If you intend to deprecate username lookup
        // you might remove or refactor this method as well, or update its name.
        // For now, it's fine as long as findByEmail is used in login.
        return authenticationRepository.findByUsername(username);
    }
}