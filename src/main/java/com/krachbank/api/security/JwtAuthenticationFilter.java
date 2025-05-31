// src/main/java/com/krachbank/api/security/JwtAuthenticationFilter.java
package com.krachbank.api.security;

import com.krachbank.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull; // Keep this for clarity and nullable analysis
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Your UserDetailsServiceImpl

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException { // <--- The correct throws clause is essential
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // If no Authorization header or it doesn't start with "Bearer", proceed with the filter chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // Extract the JWT token (after "Bearer ")
        username = jwtService.extractUsername(jwt); // Extract username from token

        // If username is found and no authentication is currently set in the SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load UserDetails for the extracted username
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validate the token against the loaded UserDetails
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // If token is valid, create an authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials are set to null because the token itself is the credential
                        userDetails.getAuthorities() // Get user roles/authorities
                );
                // Set authentication details from the request
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Set the authentication object in the SecurityContextHolder
                // This marks the user as authenticated for the current request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Continue with the rest of the filter chain
        filterChain.doFilter(request, response);
    }
}