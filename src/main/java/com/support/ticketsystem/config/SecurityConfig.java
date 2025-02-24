package com.support.ticketsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(auth -> auth
                        .requestMatchers("/api/tickets/**").permitAll() // Allow access to tickets API
                        .anyRequest().authenticated() // Secure other endpoints
                )
                .formLogin().disable() // Disable form login
                .httpBasic().disable() // Disable basic authentication
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/tickets/**")) // Disable CSRF for tickets
                .cors(); // Enable CORS support

        return http.build();
    }
}
