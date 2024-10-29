package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "liked_recipe")
public class LikedRecipe {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private ObjectId RecipeId;

    public LikedRecipe(ObjectId userId, ObjectId recipeId) {
        UserId = userId;
        RecipeId = recipeId;
    }
}
