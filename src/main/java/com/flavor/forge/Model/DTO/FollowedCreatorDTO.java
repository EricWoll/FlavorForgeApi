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
public class FollowedCreatorDTO {
    private UUID userId;
    private String username;
    private String imageId;
    private int followerCount;
    private String aboutText;
    private boolean isFollowed;
}
