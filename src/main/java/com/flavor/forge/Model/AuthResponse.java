package com.flavor.forge.Model;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private UUID userId;
    private String email;
    private ERole role;
    private String imageId;
}
