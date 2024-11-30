package com.flavor.forge.Model.Response;

import lombok.Data;

import java.util.List;

@Data
public class RecipeWithUser {

    private String recipeId;
    private String recipeName;
    private String recipeDescription;
    private List<Object> ingredients;
    private List<String> steps;
    private String imageId;
    private int likesCount;

    private String creatorId;
    private String creator;
}
