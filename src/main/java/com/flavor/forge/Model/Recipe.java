package com.flavor.forge.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Document(collection = "recipe")
public class Recipe {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private String RecipeName;
    private String RecipeDescription;
    private List<String> Ingredients;
    private List<String> Steps;
    private String ImageId;
    private int LikesCount;

    public Recipe(ObjectId userId, String recipeName, String recipeDescription, List<String> ingredients, List<String> steps, String imageId, int likesCount) {
        UserId = userId;
        RecipeName = recipeName;
        RecipeDescription = recipeDescription;
        Ingredients = ingredients;
        Steps = steps;
        ImageId = imageId;
        LikesCount = likesCount;
    }

    public void addLike() {
        LikesCount++;
    }

    public void removeLike() {
        LikesCount--;
    }
}
