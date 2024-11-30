package com.flavor.forge.Model.Response;

import com.flavor.forge.Model.ERole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicCreatorResponse {
    private String creatorId;
    private String creatorUsername;
    private String creatorImageId;
    private int followerCount;
    private String creatorAboutText;
    private ERole creatorRole;

    private boolean isFollowed;
}
