package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "followed_creator")
public class FollowedCreator {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private ObjectId CreatorId;

    public FollowedCreator(ObjectId userId, ObjectId creatorId) {
        UserId = userId;
        CreatorId = creatorId;
    }
}
