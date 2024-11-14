package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "recipe")
public class Recipe {

    @MongoId
    private ObjectId id;
    private String recipeId;
    private String userId;
    private String recipeName;
    private String recipeDescription;
    private List<Object> ingredients;
    private List<String> steps;
    private String imageId;
    private int likesCount;

    public Recipe(String userId, String recipeName, String recipeDescription, List<Object> ingredients, List<String> steps, String imageId, int likesCount) {
        this.recipeId = UUID.randomUUID().toString();
        this.userId = userId;
        this.recipeName = recipeName;
        this.recipeDescription = recipeDescription;
        this.ingredients = ingredients;
        this.steps = steps;
        this.imageId = imageId;
        this.likesCount = likesCount;
    }

    public void addLike() {
        this.likesCount++;
    }

    public void removeLike() {
        this.likesCount--;
    }
}
