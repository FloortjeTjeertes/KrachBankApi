package com.krachBank.api.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.krachBank.api.dto.UserDTO;
import com.krachBank.api.service.AuthenticationService;

@RestController
@RequestMapping("/authenticate")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody UserDTO userDTO) {
        // Call the service to register the user and return the result
        return authenticationService.register(userDTO);
    }
}
