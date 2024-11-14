package com.flavor.forge.Model;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String email;
    private String userId;
    private ERole role;
    private String imageId;
}
