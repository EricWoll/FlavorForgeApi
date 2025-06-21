package com.flavor.forge.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class PrivateUserDTO {
    private String userId;
    private String username;
    private String email;
    private String imageId;
    private int followerCount;
    private String aboutText;
}
