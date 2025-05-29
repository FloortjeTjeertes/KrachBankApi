package com.krachbank.api.configuration;

import com.krachbank.api.security.JwtAuthenticationFilter;
import jakarta.servlet.Filter; // Required for type casting jwtAuthFilter in addFilterBefore
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // For modern csrf disable
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService; // Needed for AuthenticationProvider
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; // Use PasswordEncoder interface
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private final UserDetailsService userDetailsService; // Injected from SecurityConfig
    private final JwtAuthenticationFilter jwtAuthFilter; // Injected from SecurityConfig

    // Constructor injection for UserDetailsService and JwtAuthenticationFilter
    public WebSecurityConfiguration(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Defines the PasswordEncoder bean.
     * Retains the BCryptPasswordEncoder with strength 12 from the original WebSecurityConfiguration.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configures the AuthenticationProvider.
     * Uses DaoAuthenticationProvider with the injected UserDetailsService and the defined PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager bean.
     * Essential for handling authentication requests (e.g., login).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the SecurityFilterChain for HTTP security.
     * This combines rules from both original configuration files.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless JWT-based APIs (modern syntax)
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Allow H2 console frame
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints from both original configurations
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                        .requestMatchers("/api/auth/**").permitAll()   // Covers login/signup and other auth-related endpoints
                        .requestMatchers("/api/users").permitAll()     // Allow unauthenticated POST for user creation
                        // If "/api/authenticate/register" or "/api/authenticate/login" are not covered by "/api/auth/**",
                        // explicitly add them here:
                        // .requestMatchers("/api/authenticate/register", "/api/authenticate/login").permitAll()
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions for JWT
                )
                .authenticationProvider(authenticationProvider()) // Set your custom authentication provider
                // Add your custom JWT filter before Spring Security's default username/password filter
                .addFilterBefore((Filter) jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Global CORS configuration bean.
     * Retained from the original WebSecurityConfiguration.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173") // Allow requests from your Vue.js development server
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Include OPTIONS for pre-flight requests
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}