package com.RFE.backend.RemoteFileExplorer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — this is a stateless REST API, not a browser form app
            .csrf(AbstractHttpConfigurer::disable)
            // Stateless session — no HTTP session needed for REST
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Device endpoints are called by device agents, no user auth needed
                .requestMatchers("/api/v1/devices/**").permitAll()
                // Web client endpoints are public for now (remove for production)
                .requestMatchers("/api/v1/web/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
