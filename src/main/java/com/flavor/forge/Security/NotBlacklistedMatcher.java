package com.flavor.forge.Security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

import java.util.List;

public class NotBlacklistedMatcher implements RequestMatcher {
    private static final List<String> blacklistedPatterns = List.of(
            "/api/v2/users/update/**",
            "/api/v2/users/delete/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = request.getRequestURI();
        return blacklistedPatterns.stream().noneMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
