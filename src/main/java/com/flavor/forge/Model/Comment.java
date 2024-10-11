package com.flavor.forge.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
@Document(collection = "comment")
public class Comment {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private ObjectId AttachedId;
    private String CommentText;
}
