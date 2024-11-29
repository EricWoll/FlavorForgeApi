package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "liked_recipe")
public class LikedRecipe {

    @MongoId
    private ObjectId id;
    private String userId;
    private String recipeId;

    public LikedRecipe(String userId, String recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
    }
}
