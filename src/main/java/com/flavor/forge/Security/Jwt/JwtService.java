package com.flavor.forge.Security.Jwt;

import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.UserRepo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Autowired
    private UserRepo userRepo;

    @Value("${forge.app.jwtSecret}")
    private String jwtSecret;

    @Value("${forge.app.jwtExpireMs}")
    private long jwtExpireMs;

    @Value("${forge.app.jwtRefreshExpireMs}")
    private long jwtRefreshExpireMs;

    public String generateJwtToken(User user) {
        return buildToken(new HashMap<>(), user, jwtExpireMs);
    }

    public String generateJwtRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, jwtRefreshExpireMs);
    }

    private String buildToken(Map<String, Object> claims,
                                 User user,
                              long expiration
    ) {
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration((new Date(System.currentTimeMillis() + expiration )))
                .and()
                .signWith(getKey())
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsername(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);

        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Malformed JWT");

        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Expired JWT");

        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException("Unsupported JWT");

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Illegal Argument JWT");
        }

    }

    public String trimJWTBearerToken(String bearerToken) {
        return bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : bearerToken;
    }

    public boolean validateAccessTokenCredentials(String bearerToken) {
        String accessToken = trimJWTBearerToken(bearerToken);

        if (!validateToken(accessToken)) {
            throw new BadCredentialsException("Bad Credentials");
        }
        return true;
    }

    public boolean validateAccessTokenAgainstFoundUserId(String bearerToken, UUID foundUser) {
        String accessToken = trimJWTBearerToken(bearerToken);
        String queuedUsername = getUsername(accessToken);

        User userQueued = userRepo.findByUserId(foundUser).orElseThrow(() -> new UserNotFoundException("User Not Found!"));
        if (!queuedUsername.equals(userQueued.getUsername())) {
            throw new BadCredentialsException("User queued for Adding is not the same as the Active user!");
        }
        return true;
    }

    public boolean validateAccessTokenAgainstFoundUsername(String bearerToken, String foundUsername) {
        String accessToken = trimJWTBearerToken(bearerToken);
        String queuedUsername = getUsername(accessToken);

        if (!queuedUsername.equals(foundUsername)) {
            throw new BadCredentialsException("User queued for Adding is not the same as the Active user!");
        }
        return true;
    }

    public <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private Date getExpireDateFromToken(String token) {
        return getClaimsFromToken(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpireDateFromToken(token);
        return expiration.before(new Date());
    }

}
