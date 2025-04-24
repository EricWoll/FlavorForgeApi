package com.flavor.forge.Model.DTO;

import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavor.forge.Model.Ingredient;
import lombok.*;

import java.util.List;


@Data
public class RecipeWithCreatorDTO {
    private UUID recipeId;
    private UUID creatorId;
    private String creatorImageId;
    private String creatorUsername;
    private String recipeName;
    private String recipeImageId;
    private String recipeDescription;
    private List<Ingredient> ingredients;
    private List<String> steps;
    private int likesCount;
    private int viewsCount;
    private boolean isLiked;

    // Constructor that takes the values from the query result
    public RecipeWithCreatorDTO(UUID recipeId, UUID creatorId, String creatorImageId, String creatorUsername,
                                String recipeName, String recipeImageId, String recipeDescription,
                                String ingredientsJson, List<String> stepsJson, int likesCount,
                                int viewsCount, boolean isLiked) {
        this.recipeId = recipeId;
        this.creatorId = creatorId;
        this.creatorImageId = creatorImageId;
        this.creatorUsername = creatorUsername;
        this.recipeName = recipeName;
        this.recipeImageId = recipeImageId;
        this.recipeDescription = recipeDescription;
        this.ingredients = parseIngredients(ingredientsJson);
        this.steps = stepsJson;
        this.likesCount = likesCount;
        this.viewsCount = viewsCount;
        this.isLiked = isLiked;
    }

    private List<Ingredient> parseIngredients(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<List<Ingredient>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ingredients JSON", e);
        }
    }
}


