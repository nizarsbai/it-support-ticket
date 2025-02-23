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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/tickets/**").permitAll() // Autorise l'accès à l'API des tickets
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login.disable()) // Désactive le formulaire de login
                .httpBasic(httpBasic -> httpBasic.disable()) // Désactive l'authentification basique
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/tickets/**")); // Désactive CSRF uniquement pour l'API tickets

        return http.build();
    }
}