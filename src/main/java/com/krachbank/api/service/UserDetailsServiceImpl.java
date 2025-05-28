package com.krachbank.api.security;

import com.krachbank.api.models.User;
import com.krachbank.api.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthenticationRepository authenticationRepository;

    @Autowired
    public UserDetailsServiceImpl(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieve the user from your repository
        User user = authenticationRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Note: For a real application, you'd fetch user roles/authorities
        // and pass them to Spring Security's User constructor.
        // For simplicity, we are creating a basic UserDetails object directly from your User model
        // that will implement org.springframework.security.core.userdetails.UserDetails
        return user; // Your User model will implement UserDetails
    }
}