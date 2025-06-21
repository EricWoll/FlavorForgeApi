package com.flavor.forge.Security.SharedSecret;

import com.flavor.forge.Model.ERole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SharedSecretFilter extends OncePerRequestFilter {
    @Value("${forge.app.nextJs.apiKey}")
    private String sharedSecret;

    private static final List<String> protectedPatterns = List.of(
            "/api/v2/users/update/**",
            "/api/v2/users/delete/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean requiresAuth = protectedPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (!requiresAuth) {
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                System.out.println("Exception in SharedSecretFilter: " + e.getMessage());
                throw e;
            }
            return;
        }

        String secretHeader = request.getHeader("nextjs-shared-secret");

        if (sharedSecret.equals(secretHeader)) {
            UsernamePasswordAuthenticationToken systemAuth =
                    new UsernamePasswordAuthenticationToken(
                            "system", null, ERole.SYSTEM.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(systemAuth);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                System.out.println("Authorities: " + auth.getAuthorities());
            } else {
                System.out.println("No authentication present in context.");
            }

            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
        }
    }
}
