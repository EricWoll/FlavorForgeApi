package com.flavor.forge.Security.Clerk;

import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.UserRepo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * This filter intercepts requests to verify JWT tokens issued by Clerk.
 * It extracts the user information from the token and sets the authentication in the security context.
 */
@Component
@RequiredArgsConstructor
public class ClerkAuthFilter extends OncePerRequestFilter {

    private final ClerkService clerkService;
    private final UserRepo userRepo;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String sharedSecret = request.getHeader("nextjs-shared-secret");
        if (sharedSecret != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = clerkService.verifyToken(token);
                String clerkUserId = claims.getSubject();

                User user = userRepo.findByUserId(clerkUserId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                Authentication auth = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                logger.warn("JWT verification failed: " + e.getMessage());
                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Attempt to find a user by Clerk user ID, retrying for a short period if not found immediately.
     *
     * @param clerkUserId The Clerk user ID from the JWT token.
     * @return The User if found; otherwise, null.
     */
    private User findUserWithRetry(String clerkUserId) {
        int maxRetries = 5;
        int delayMs = 200; // Delay in milliseconds between retries

        for (int i = 0; i < maxRetries; i++) {
            Optional<User> userOpt = userRepo.findByUserId(clerkUserId);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }
}
