package com.iron.testutils;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public class SecurityTestUtils {

    public static RequestPostProcessor jwt(String login) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("preferred_username", login)
                .claim("scope", "read write")
                .build();
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt);
    }

    public static RequestPostProcessor jwt(String login, String... scopes) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("preferred_username", login)
                .claim("scope", String.join(" ", scopes))
                .build();
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt);
    }

    public static RequestPostProcessor jwtWithClaims(String login, String claimName, Object claimValue) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("preferred_username", login)
                .claim("scope", "read write")
                .claim(claimName, claimValue)
                .build();
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt);
    }
}
