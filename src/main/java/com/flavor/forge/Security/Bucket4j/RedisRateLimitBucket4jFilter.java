package com.flavor.forge.Security.Bucket4j;

import com.flavor.forge.Model.ERole;
import com.flavor.forge.Security.Jwt.JwtConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class RedisRateLimitBucket4jFilter extends OncePerRequestFilter {

    @Autowired
    private ProxyManager<String> proxyManager;

    @Autowired
    private Map<String, Map<String, Supplier<BucketConfiguration>>> ratePolicies;

    @Value("${forge.app.jwtSecret}")
    private String jwtSecret;

    @Autowired
    private JwtConfig jwtConfig;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = normalizePath(request.getRequestURI());
        String matchedPolicyKey = findMatchingPolicyKey(path);

        String sharedSecret = request.getHeader("nextjs-shared-secret");
        if (sharedSecret != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (matchedPolicyKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Supplier<BucketConfiguration>> userPolicies = ratePolicies.get(matchedPolicyKey);


        String token = extractBearerToken(request.getHeader("Authorization"));
        String userType = ERole.ANON.getRole(); // default
        String userId;

        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null) {
            ipAddr = request.getRemoteAddr();
        }
        userId = ipAddr;

        if (token == null || token.isBlank()) {
            // No token, anonymous user
            userType = ERole.ANON.getRole();
            userId = ipAddr;
        } else {
            try {
                Jwt jwt = jwtConfig.jwtDecoder().decode(token);
                userId = jwt.getSubject(); // Clerk userId from "sub" claim

                Object roleClaim = jwt.getClaim("userRole"); // or "publicMetadata.role" based on your config
                if (roleClaim instanceof String role) {
                    userType = role.toUpperCase(); // e.g., FREE, PAID
                }

            } catch (JwtException e) {
                // Invalid Clerk token; fallback to ANON
                userType = ERole.ANON.getRole();
                userId = ipAddr;
            }
        }

        // Get per-path, per-userType rate limit config
        Supplier<BucketConfiguration> configSupplier = userPolicies.get(userType);

        String bucketKey = String.format("rate:%s:%s:%s", path, userType, userId);

        if (configSupplier == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = proxyManager.builder().build(bucketKey, configSupplier);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/favicon.ico") || path.startsWith("/static/") || path.startsWith("/css/") || path.startsWith("/js/");
    }

    private String normalizePath(String uri) {
        return uri.replaceAll("/+$", "");
    }

    private String findMatchingPolicyKey(String uri) {
        return ratePolicies.keySet().stream()
                .filter(pattern -> pathMatcher.match(pattern, uri))
                .findFirst()
                .orElse(null);
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7);
    }
}
