package com.krachbank.api.controllers;

import com.krachbank.api.dto.*;
import com.krachbank.api.exceptions.InvalidCredentialsException;
import com.krachbank.api.service.AuthenticationService;
import com.krachbank.api.service.JwtService;
import com.krachbank.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService; // Inject the service


    public AuthenticationController(UserService userService, AuthenticationService authenticationService) {
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
            userDTO.setIsAdmin(false);
            userDTO.setUsername(registerRequest.getFirstName() + registerRequest.getLastName());
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully with email: " + createdUser.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResultDTO> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            AuthenticationResultDTO result = authenticationService.login(loginRequest);
            return ResponseEntity.ok(result); // Return the full AuthenticationResultDTO
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or return an error DTO
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}