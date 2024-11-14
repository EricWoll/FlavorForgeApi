package com.flavor.forge.Model.Response;

import com.flavor.forge.Model.ERole;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PublicUserResponse {

    private String userId;
    private String username;
    private String imageId;
    private int followerCount;
    private String aboutText;
    private ERole role;
}
