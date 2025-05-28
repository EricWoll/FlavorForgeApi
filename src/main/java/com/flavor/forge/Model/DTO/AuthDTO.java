package com.flavor.forge.Model.DTO;

import com.flavor.forge.Model.ERole;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthDTO {
    private String accessToken;
    private String refreshToken;
    private String username;
    private UUID userId;
    private String email;
    private ERole role;
    private String imageId;
}
