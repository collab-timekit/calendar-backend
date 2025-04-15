package com.calendar.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class JwtTokenProvider {

    public Authentication getAuthentication(String token) {
        if ("mock-jwt-token".equals(token)) {
            return new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList());
        }
        return null;
    }

    public String generateToken(Authentication authentication) {
        return "mock-jwt-token";
    }
}
