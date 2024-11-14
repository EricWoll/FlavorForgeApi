package com.flavor.forge.Security;

import com.flavor.forge.Model.ERole;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authHttp -> {

                            // Not Logged In
                            authHttp.requestMatchers(
                                    HttpMethod.GET,
                                    "/api/v1/search/recipes",
                                    "/api/v1/search/recipes/**",
                                    "/api/v1/comments",
                                    "/api/v1/comments/**",
                                    "/api/v1/users/**",
                                    "/api/v1/recipes/**",
                                    "/api/v1/images/**"
                            ).permitAll();
                            authHttp.requestMatchers(
                                    HttpMethod.POST,
                                    "/api/v1/auth/login",
                                    "/api/v1/auth/register"
                            ).permitAll();

                            // Logged In
                            authHttp.requestMatchers(
                                    HttpMethod.GET,
                                    "/api/v1/auth/refresh"
                            ).hasRole(ERole.FREE.getRole());
                            authHttp.requestMatchers(
                                    HttpMethod.PUT,
                                    "/api/v1/comments/**",
                                    "/api/v1/recipes/**",
                                    "/api/v1/users/**",
                                    "/api/v1/images/**"
                            ).hasRole(ERole.FREE.getRole());
                            authHttp.requestMatchers(
                                    HttpMethod.POST,
                                    "/api/v1/auth/refresh",
                                    "/api/v1/comments/**",
                                    "/api/v1/comments",
                                    "/api/v1/recipes/**",
                                    "/api/v1/recipes",
                                    "/api/v1/users/**",
                                    "/api/v1/images/**"
                            ).hasRole(ERole.FREE.getRole());
                            authHttp.requestMatchers(
                                    HttpMethod.DELETE,
                                    "/api/v1/comments/**",
                                    "/api/v1/recipes/**",
                                    "/api/v1/users/**",
                                    "/api/v1/images/**"
                            ).hasRole(ERole.FREE.getRole());
                        }
                )
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }
        };
    }
}
