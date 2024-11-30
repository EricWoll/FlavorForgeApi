package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Data
@Document(collection = "followed_creator")
public class FollowedCreator {

    @MongoId
    private ObjectId id;
    private String followedId;
    private String userId;
    private String creatorId;

    public FollowedCreator(String userId, String creatorId) {
        this.followedId = UUID.randomUUID().toString();
        this.userId = userId;
        this.creatorId = creatorId;
    }
}
