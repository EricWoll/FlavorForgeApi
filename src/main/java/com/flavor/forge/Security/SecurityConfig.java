package com.flavor.forge.Security;

import com.flavor.forge.Model.ERole;
import com.flavor.forge.Security.Bucket4j.RedisRateLimitBucket4jFilter;
import com.flavor.forge.Security.Jwt.JwtFilter;
import com.flavor.forge.Security.Service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private RedisRateLimitBucket4jFilter redisBucket4jFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authHttp -> authHttp

                        // Allowing public access to login and signup endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v2/auth/signup",
                                "/api/v2/auth/login"
                        ).permitAll()

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
                                "api/v2/comments/update/**",
                                "/api/v2/recipes/update/**",
                                "/api/v2/users/update/**"
                        ).hasRole(ERole.FREE.getRole())

                        // Restrict Access through ROLES to DELETE
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v2/comments/delete/**",
                                "/api/v2/recipes/delete/**",
                                "/api/v2/recipes/liked/delete/**",
                                "/api/v2/users/delete/**",
                                "/api/v2/users/followed/delete/**",
                                "/api/v2/images/delete/**"
                        ).hasRole(ERole.FREE.getRole())

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)  // Ensure JWT filter only applies to non-public endpoints
                .addFilterBefore(redisBucket4jFilter, JwtFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
