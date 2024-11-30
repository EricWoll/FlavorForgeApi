package com.flavor.forge.Model.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeWithUserLoggedIn {
    private String creatorId;
    private String creatorUsername;

    private String recipeId;
    private String recipeName;
    private String recipeDescription;
    private List<Object> ingredients;
    private List<String> steps;
    private String imageId;
    private int likesCount;

    private boolean isFollowing;
}
