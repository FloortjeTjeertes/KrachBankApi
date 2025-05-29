package com.krachbank.api.controllers;

import com.krachbank.api.dto.LoginRequest;
import com.krachbank.api.dto.UserDTO;
import com.krachbank.api.models.User;
import com.krachbank.api.dto.RegisterRequest;
import com.krachbank.api.dto.JwtResponse;
import com.krachbank.api.security.JwtService;
import com.krachbank.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
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
            // userDTO.setPhoneNumber(registerRequest.getPhoneNumber()); // Uncomment if RegisterRequest has phoneNumber

            // Map first name + last name to username
            userDTO.setUsername(registerRequest.getFirstName() + " " + registerRequest.getLastName()); // <--- MODIFIED THIS LINE

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
    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Authenticate the user using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtService.generateToken((UserDetails) authentication.getPrincipal());

        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}