package com.flavor.forge.Security;

import com.flavor.forge.Model.ERole;
import com.flavor.forge.Security.Bucket4j.RedisRateLimitBucket4jFilter;
import com.flavor.forge.Security.Clerk.ClerkAuthFilter;
import com.flavor.forge.Security.Jwt.JwtConfig;
import com.flavor.forge.Security.SharedSecret.SharedSecretFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

    @Autowired
    private JwtConfig jwtConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authHttp -> authHttp
                        // Public signup endpoint - no authentication required
                        .requestMatchers("/api/v2/auth/signup").permitAll()

                        // Internal system endpoints - require SYSTEM role via shared secret
                        .requestMatchers("/api/v2/users/update/**", "/api/v2/users/delete/**")
                        .hasRole(ERole.SYSTEM.getRole())

                        // Public GET endpoints - no authentication but rate limited
                        .requestMatchers(HttpMethod.GET,
                                "/api/v2/recipes/search/**",
                                "/api/v2/comments/search/**",
                                "/api/v2/users/search/**",
                                "/api/v2/images"
                        ).permitAll()

                        // Authenticated GET endpoints - require FREE role minimum
                        .requestMatchers(HttpMethod.GET,
                                "/api/v2/users/profile/**",
                                "/api/v2/recipes/liked/search/**",
                                "/api/v2/users/followed/search/**"
                        ).hasRole(ERole.FREE.getRole())

                        // Authenticated POST endpoints - require FREE role minimum
                        .requestMatchers(HttpMethod.POST,
                                "/api/v2/auth/refresh",
                                "/api/v2/comments/create",
                                "/api/v2/recipes/create",
                                "/api/v2/recipes/liked/add/**",
                                "/api/v2/users/followed/add/**",
                                "/api/v2/images/upload"
                        ).hasRole(ERole.FREE.getRole())

                        // Authenticated PUT endpoints - require FREE role minimum
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v2/comments/update/**",
                                "/api/v2/recipes/update/**"
                        ).hasRole(ERole.FREE.getRole())

                        // Authenticated DELETE endpoints - require FREE role minimum
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v2/comments/delete/**",
                                "/api/v2/recipes/delete/**",
                                "/api/v2/recipes/liked/delete/**",
                                "/api/v2/users/followed/delete/**",
                                "/api/v2/images/delete/**"
                        ).hasRole(ERole.FREE.getRole())

                        // All other API requests require authentication
                        .requestMatchers("/api/**").authenticated()

                        // Deny all other requests
                        .anyRequest().denyAll()
                )
                // Configure JWT authentication for Clerk
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtConfig.jwtDecoder())
                                .jwtAuthenticationConverter(jwtConfig.jwtAuthenticationConverter())
                        )
                )
                // Add conditional filters
                .addFilterBefore(createConditionalSharedSecretFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(createConditionalClerkAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(createConditionalRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private OncePerRequestFilter createConditionalSharedSecretFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String uri = request.getRequestURI();

                // Only apply shared secret filter to internal endpoints
                if (uri.matches("/api/v2/users/update/.*") ||
                        uri.matches("/api/v2/users/delete/.*")) {
                    sharedSecretFilter.doFilter(request, response, filterChain);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }

    private OncePerRequestFilter createConditionalClerkAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String uri = request.getRequestURI();
                String method = request.getMethod();

                // Skip Clerk auth for public endpoints
                if ("/api/v2/auth/signup".equals(uri) ||
                        (uri.matches("/api/v2/users/update/.*") || uri.matches("/api/v2/users/delete/.*") || uri.startsWith("/api/webhooks/")) ||
                        ("GET".equals(method) && (uri.startsWith("/api/v2/recipes/search") ||
                                uri.startsWith("/api/v2/comments/search/") ||
                                uri.startsWith("/api/v2/users/search/") ||
                                uri.equals("/api/v2/images")))) {
                    filterChain.doFilter(request, response);
                } else if (uri.startsWith("/api/")) {
                    // Apply Clerk auth to all other API endpoints
                    clerkAuthFilter.doFilter(request, response, filterChain);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }

    private OncePerRequestFilter createConditionalRateLimitFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // Apply rate limiting to ALL requests
                redisBucket4jFilter.doFilter(request, response, filterChain);
            }
        };
    }
}
