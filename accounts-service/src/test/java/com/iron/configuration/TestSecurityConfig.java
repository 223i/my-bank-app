package com.iron.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return new TestJwtDecoder();
    }

    private static class TestJwtDecoder implements JwtDecoder {
        
        @Override
        public Jwt decode(String token) throws JwtValidationException {
            // For testing, we'll create a valid JWT from any token string
            // In real scenarios, you might want to validate specific token formats
            
            // Extract username from token or use default
            String username = extractUsernameFromToken(token);
            
            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("preferred_username", username)
                    .claim("scope", "read write")
                    .claim("iss", "test-issuer")
                    .claim("aud", "test-audience")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
        
        private String extractUsernameFromToken(String token) {
            // Simple extraction for testing - in real scenarios you'd decode JWT
            if (token.contains("testuser")) return "testuser";
            if (token.contains("otheruser1")) return "otheruser1";
            if (token.contains("otheruser2")) return "otheruser2";
            if (token.contains("nonexistent")) return "nonexistent";
            if (token.contains("admin")) return "admin";
            
            // Default fallback for any other token
            return "testuser";
        }
    }
}
