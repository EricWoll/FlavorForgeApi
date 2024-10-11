package com.flavor.forge.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
@Document(collection = "liked_recipe")
public class LikedRecipe {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private ObjectId RecipeId;
}
