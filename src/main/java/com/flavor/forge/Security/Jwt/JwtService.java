package com.flavor.forge.Security.Jwt;

import com.flavor.forge.Model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${adopt.app.jwtSecret}")
    private String jwtSecret;

    @Value("${adopt.app.jwtExpireMs}")
    private long jwtExpireMs;

    @Value("${adopt.app.jwtRefreshExpireMs}")
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
