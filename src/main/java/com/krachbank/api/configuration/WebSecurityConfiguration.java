package com.krachbank.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // We need to do this to allow POST requests
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // allow h2-console
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        // .anyRequest().authenticated()
                        // all other requests need authentication
                        .permitAll()
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable()) // disable CSRF for h2-console
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // disable frame
                                                                                                   // options for
                                                                                                   // h2-console because
                                                                                                   // of firefox
        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
