package com.flavor.forge.Model;

import lombok.*;
import org.bson.types.ObjectId;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String email;
    private ObjectId userId;
    private ERole role;
    private String imageId;
}
