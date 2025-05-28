package com.flavor.forge.Security.Bucket4j;

import com.flavor.forge.Model.ERole;
import com.flavor.forge.Security.Jwt.JwtService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class RedisRateLimitBucket4jFilter extends OncePerRequestFilter {

    @Autowired
    private ProxyManager<String> proxyManager;

    @Autowired
    private Map<String, Map<String, Supplier<BucketConfiguration>>> ratePolicies;

    @Autowired
    private JwtService jwtService;

    @Value("${forge.app.jwtSecret}")
    private String jwtSecret;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = normalizePath(request.getRequestURI());
        System.out.println("Path Type: " + path);
        String matchedPolicyKey = findMatchingPolicyKey(path);

        if (matchedPolicyKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Map<String, Supplier<BucketConfiguration>> userPolicies = ratePolicies.get(matchedPolicyKey);


        String token = jwtService.trimJWTBearerToken(request.getHeader("Authorization"));
        String userType = ERole.ANON.getRole(); // default role
        String userId = null;

        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null) {
            ipAddr = request.getRemoteAddr();
        }

        if (token == null || token.isBlank()) {
            // No token, anonymous user
            userType = ERole.ANON.getRole();
            userId = ipAddr;
        } else {
            try {
                String parsedRole = jwtService.getClaimsFromToken(token, claims -> claims.get("userRole", String.class));
                String parsedUserId = jwtService.getClaimsFromToken(token, claims -> claims.get("userId", String.class));

                if (parsedRole != null && parsedUserId != null) {
                    userType = parsedRole;
                    userId = parsedUserId;
                } else {
                    // Token is invalid or incomplete, treat as ANON
                    userType = ERole.ANON.getRole();
                    userId = ipAddr;
                }
            } catch (JwtException e) {
                // Treat invalid token as ANON instead of rejecting
                userType = ERole.ANON.getRole();
                userId = ipAddr;
            }
        }

        System.out.printf("Resolved userType=%s, userId=%s%n", userType, userId);


        // Get per-path, per-userType rate limit config
        Supplier<BucketConfiguration> configSupplier = userPolicies.get(userType);

        String bucketKey = String.format("rate:%s:%s:%s", path, userType,  userId);

        if (configSupplier == null) {
            System.out.println("No config found for userType: " + userType);
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = proxyManager.builder().build(bucketKey, configSupplier);

        if (bucket.tryConsume(1)) {
            System.out.println("TryConsume result: true");
            System.out.println("Bucket Amount: " + bucket.getAvailableTokens());
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
            return;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // skip filtering for favicon or other static assets
        if (path.equals("/favicon.ico") || path.startsWith("/static/") || path.startsWith("/css/") || path.startsWith("/js/")) {
            return true;
        }
        return false;
    }

    private String normalizePath(String uri) {
        return uri.replaceAll("/+$", ""); // remove trailing slashes
    }

    private String findMatchingPolicyKey(String uri) {
        return ratePolicies.keySet().stream()
                .filter(pattern -> pathMatcher.match(pattern, uri))
                .findFirst()
                .orElse(null);
    }
}
