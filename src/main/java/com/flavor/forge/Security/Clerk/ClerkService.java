package com.flavor.forge.Security.Clerk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavor.forge.Model.ERole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClerkService {

    @Value("${forge.app.clerk.jwksUrl}")
    private String clerkJwksUrl;

    @Value("${forge.app.clerk.expectedIssuer}")
    private String clerkExpectedIssuer;

    @Value("${forge.app.clerk.apiUrl}")
    private String clerkApiUrl;

    @Value("${forge.app.clerk.apiKey}")
    private String clerkApiKey;

    private PublicKey cachedKey;
    private String cachedKid;

    public Claims verifyToken(String token) throws Exception {
        String kid = getKidFromToken(token);

        if (cachedKey == null || !kid.equals(cachedKid)) {
            cachedKey = fetchPublicKey(kid);
            cachedKid = kid;
        }

        return Jwts.parser()
                .verifyWith(cachedKey)
                .requireIssuer(clerkExpectedIssuer)
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PublicKey fetchPublicKey(String kid) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> jwks = objectMapper.readValue(
                URI.create(clerkJwksUrl).toURL(),
                new TypeReference<Map<String, Object>>() {}
        );

        List<Map<String, Object>> keys = objectMapper.convertValue(
                jwks.get("keys"),
                new TypeReference<List<Map<String, Object>>>() {}
        );

        for (Map<String, Object> key : keys) {
            if (key.get("kid").equals(kid)) {
                String n = (String) key.get("n");
                String e = (String) key.get("e");

                RSAPublicKeySpec spec = new RSAPublicKeySpec(
                        new java.math.BigInteger(1, Base64.getUrlDecoder().decode(n)),
                        new java.math.BigInteger(1, Base64.getUrlDecoder().decode(e))
                );

                return KeyFactory.getInstance("RSA").generatePublic(spec);
            }
        }

        throw new IllegalArgumentException("Public key with kid " + kid + " not found");
    }

    private String getKidFromToken(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> header = objectMapper.readValue(
                headerJson,
                new TypeReference<Map<String, Object>>() {}
        );

        return (String) header.get("kid");
    }

    public ERole fetchUserRoleFromClerk(String clerkUserId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clerkApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                clerkApiUrl + "users/" + clerkUserId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> userData = response.getBody();
        if (userData == null) {
            return ERole.FREE;  // Default fallback role
        }

        // First, check if the token already includes a 'userRole' field
        if (userData.containsKey("userRole")) {
            String role = (String) userData.get("userRole");
            return mapRoleToERole(role);
        }

        // Otherwise, check public_metadata for the role.
        Map<String, Object> publicMetadata = (Map<String, Object>) userData.get("private_metadata");
        if (publicMetadata != null && publicMetadata.containsKey("role")) {
            String role = (String) publicMetadata.get("role");
            return mapRoleToERole(role);
        }

        // If role data is not available, fallback to FREE
        return ERole.FREE;
    }

    private ERole mapRoleToERole(String role) {
        if (role == null) return ERole.FREE;
        return switch (role.toLowerCase()) {
            case "premium" -> ERole.PREMIUM;
            case "pro" -> ERole.PRO;
            default -> ERole.FREE;
        };
    }

    public void updateUserRoleInClerk(String clerkUserId, ERole role) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clerkApiKey);
        headers.set("Content-Type", "application/json");

        // Prepare payload to update private metadata with the role
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("role", role.toString());
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("private_metadata", metadata);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updatePayload, headers);

        // Construct the URL: make sure clerkApiUrl ends with a slash (or add one) so that endpoint gets built correctly.
        String url = clerkApiUrl + "users/" + clerkUserId;

        // Clerk's API expects a PATCH request to update a user record
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Map.class);

        // Check if the update is successful
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Failed to update Clerk user role: " + response.getStatusCode());
        }
    }

}
