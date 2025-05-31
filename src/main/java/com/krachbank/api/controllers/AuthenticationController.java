package com.krachbank.api.controllers;

import com.krachbank.api.dto.*;
import com.krachbank.api.exceptions.InvalidCredentialsException;
import com.krachbank.api.service.AuthenticationService;
import com.krachbank.api.service.JwtService;
import com.krachbank.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationService authenticationService; // Inject the service


    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService, AuthenticationService authenticationService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(registerRequest.getEmail());
            userDTO.setPassword(registerRequest.getPassword());
            userDTO.setFirstName(registerRequest.getFirstName());
            userDTO.setLastName(registerRequest.getLastName());
            userDTO.setBSN(registerRequest.getBSN());
            userDTO.setPhoneNumber(registerRequest.getPhoneNumber());
            // userDTO.setPhoneNumber(registerRequest.getPhoneNumber()); // Uncomment if RegisterRequest has phoneNumber

            // Map first name + last name to username
            userDTO.setUsername(registerRequest.getFirstName() + registerRequest.getLastName()); // <--- MODIFIED THIS LINE

            // Debugging prints (keep them temporarily as you debug)
            System.out.println("Received registration request for email: " + registerRequest.getEmail());
            System.out.println("Password in RegisterRequest: " + registerRequest.getPassword());
            System.out.println("Username set in UserDTO: " + userDTO.getUsername());


            UserDTO createdUser = userService.createUser(userDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully with email: " + createdUser.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResultDTO> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Delegate the login logic to your AuthenticationService
            AuthenticationResultDTO result = authenticationService.login(loginRequest);
            return ResponseEntity.ok(result); // Return the full AuthenticationResultDTO
        } catch (InvalidCredentialsException e) {
            // Catch custom exception and return 401 Unauthorized or 403 Forbidden
            // As discussed, 401 is more appropriate for invalid credentials.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or return an error DTO
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}