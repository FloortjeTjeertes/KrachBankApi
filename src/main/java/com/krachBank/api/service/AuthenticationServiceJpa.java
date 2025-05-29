package com.krachBank.api.service;

import com.krachBank.api.repository.UserRepository;

@Service
public class AuthenticationServiceJpa implements AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationServiceJpa(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO Login(String username, String password) {
        // Logic for user login
        return null; // Placeholder return statement
    }

    @Override
    public User Register(User user) {
        // Logic for user registration
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        // Save user to the repository
        return userRepository.save(user);
    }

}
