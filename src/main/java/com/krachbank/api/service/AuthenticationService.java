package com.krachbank.api.service;

import com.krachbank.api.dto.AuthenticationResultDTO;
import com.krachbank.api.dto.LoginRequest;
import com.krachbank.api.dto.RegisterRequest;
import com.krachbank.api.models.User;
import com.krachbank.api.exceptions.UserAlreadyExistsException;
import com.krachbank.api.exceptions.InvalidCredentialsException;

import java.util.Optional;

public interface AuthenticationService {

    AuthenticationResultDTO register(RegisterRequest registerRequest) throws UserAlreadyExistsException;
    AuthenticationResultDTO login(LoginRequest loginRequest) throws InvalidCredentialsException;
    Optional<User> findByUsername(String username);
}