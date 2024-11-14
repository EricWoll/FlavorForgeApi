package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Data
@Document(collection = "comment")
public class Comment {

    @MongoId
    private ObjectId id;
    private String commentId;
    private String userId;
    private String attachedId;
    private String commentText;

    public Comment(String userId, String attachedId, String commentText) {
        this.commentId = UUID.randomUUID().toString();
        this.userId = userId;
        this.attachedId = attachedId;
        this.commentText = commentText;
    }
}
