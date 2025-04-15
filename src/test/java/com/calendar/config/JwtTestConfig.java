package com.calendar.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebSecurity
public class JwtTestConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF for testing
        http.csrf(AbstractHttpConfigurer::disable);

        // Configure authorization
        http.authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Allow all requests for testing
        );

        // Configure OAuth2 resource server with JWT
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
        );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Mock JwtDecoder for testing
        return mock(JwtDecoder.class);
    }
}