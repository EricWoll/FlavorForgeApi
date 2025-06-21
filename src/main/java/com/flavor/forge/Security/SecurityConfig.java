package com.flavor.forge.Security;

import com.flavor.forge.Model.ERole;
import com.flavor.forge.Security.Bucket4j.RedisRateLimitBucket4jFilter;
import com.flavor.forge.Security.Clerk.ClerkAuthFilter;
import com.flavor.forge.Security.SharedSecret.SharedSecretFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private RedisRateLimitBucket4jFilter redisBucket4jFilter;

    @Autowired
    private ClerkAuthFilter clerkAuthFilter;

    @Autowired
    private SharedSecretFilter sharedSecretFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain internalChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/v2/users/update/**", "/api/v2/users/delete/**", "/api/webhooks/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole(ERole.SYSTEM.getRole()))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(sharedSecretFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authHttp -> authHttp

                        // Allowing public access to login and signup endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v2/auth/signup"
                        ).permitAll()

                        // Allow shared-secret authenticated system requests
                        .requestMatchers(HttpMethod.PUT, "/api/v2/users/update/**")
                        .hasRole(ERole.SYSTEM.getRole())
                        .requestMatchers(HttpMethod.DELETE, "/api/v2/users/delete/**")
                        .hasRole(ERole.SYSTEM.getRole())

                        // Allow public access to GET
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v2/recipes/search",
                                "/api/v2/recipes/search/**",
                                "/api/v2/comments/search/**",
                                "/api/v2/users/search/**",
                                "/api/v2/images"
                        ).permitAll()

                        // Restrict Access through ROLES to GET
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v2/users/profile/**",
                                "/api/v2/recipes/liked/search/**",
                                "/api/v2/users/followed/search/**"
                        ).hasRole(ERole.FREE.getRole())

                        // Restrict Access through ROLES to POST
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v2/auth/refresh",
                                "/api/v2/comments/create",
                                "/api/v2/recipes/create",
                                "/api/v2/recipes/liked/add/**",
                                "/api/v2/users/followed/add/**",
                                "/api/v2/images/upload"
                        ).hasRole(ERole.FREE.getRole())

                        // Restrict Access through ROLES to PUT
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v2/comments/update/**",
                                "/api/v2/recipes/update/**"
                        ).hasRole(ERole.FREE.getRole())

                        // Restrict Access through ROLES to DELETE
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v2/comments/delete/**",
                                "/api/v2/recipes/delete/**",
                                "/api/v2/recipes/liked/delete/**",
                                "/api/v2/users/followed/delete/**",
                                "/api/v2/images/delete/**"
                        ).hasRole(ERole.FREE.getRole())

                        .anyRequest().authenticated()
                )
                // Use JWT Bearer token from Clerk
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(redisBucket4jFilter.jwtDecoder())
                                .jwtAuthenticationConverter(redisBucket4jFilter.jwtAuthenticationConverter())
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(clerkAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(redisBucket4jFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(99)
    public SecurityFilterChain fallbackChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
                .build();
    }
}
