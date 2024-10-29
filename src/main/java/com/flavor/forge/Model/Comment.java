package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "comment")
public class Comment {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private ObjectId AttachedId;
    private String CommentText;

    public Comment(ObjectId userId, ObjectId attachedId, String commentText) {
        UserId = userId;
        AttachedId = attachedId;
        CommentText = commentText;
    }
}
