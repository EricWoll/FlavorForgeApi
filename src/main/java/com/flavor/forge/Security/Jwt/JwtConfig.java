package com.flavor.forge.Security.Jwt;

import com.flavor.forge.Model.ERole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class JwtConfig {
    @Value("${forge.app.clerk.jwksUrl}")
    private String clerkJwksUrl;

    /**
     * JwtDecoder configured to validate Clerk's JWT tokens using their JWKS URL.
     * Replace `CLERK_JWKS_URL` with the correct URL from Clerk docs.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(clerkJwksUrl).build();
    }

    /**
     * Converts Clerk JWT claims into Spring Security Authorities.
     * For example, map Clerk's `role` or `claims` to `ROLE_FREE` etc.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract roles from the token claims; example assumes 'role' claim is a string or array
            Object rolesClaim = jwt.getClaim("userRole"); // or use a custom claim name from Clerk

            if (rolesClaim == null) {
                return List.of(new SimpleGrantedAuthority("ROLE_" + ERole.FREE.getRole())); // no roles found
            }

            Collection<GrantedAuthority> authorities;

            if (rolesClaim instanceof String) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rolesClaim.toString().toUpperCase()));
            } else if (rolesClaim instanceof Collection) {
                authorities = ((Collection<?>) rolesClaim).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                        .collect(Collectors.toList());
            } else {
                authorities = List.of();
            }

            return authorities;
        });

        return converter;
    }
}
