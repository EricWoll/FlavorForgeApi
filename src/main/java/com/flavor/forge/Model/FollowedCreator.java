package com.flavor.forge.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
@Document(collection = "followed_creator")
public class FollowedCreator {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private ObjectId CreatorId;
}
