package com.iron.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaSecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // проверка scope для Kafka
            List<String> scopes = jwt.getClaim("scope");
            if (scopes != null && scopes.contains("kafka:produce")) {
                return List.of(new SimpleGrantedAuthority("ROLE_KAFKA_PRODUCER"));
            }
            
            //проверка ролей realm_access
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) return Collections.emptyList();
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) return Collections.emptyList();
            return roles.stream()
                    .<org.springframework.security.core.GrantedAuthority>map(SimpleGrantedAuthority::new)
                    .toList();
        });
        return converter;
    }
}
