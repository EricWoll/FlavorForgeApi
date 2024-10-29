package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.RecipeEmptyException;
import com.flavor.forge.Exception.CustomExceptions.RecipeExistsException;
import com.flavor.forge.Exception.CustomExceptions.RecipeNotFoundException;
import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Repo.RecipeRepo;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {

    private Logger logger = LoggerFactory.getLogger(RecipeService.class);

    @Autowired
    private RecipeRepo recipeRepo;

    @Value("forge.app.noImage")
    private String noImageId;

    public Recipe findOneById(ObjectId id) {
        return recipeRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Recipe not found with Id of: {}.", id);
                    return new RecipeNotFoundException("Recipe Not Found With Id Of: " + id);
                });
    }

    public List<Recipe> findAllByUserId(ObjectId userId) {
        return recipeRepo.findAllByUserId(userId);
    }

    public Recipe createRecipe(Recipe recipe) {

        if (
                (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty())
                        || ( recipe.getRecipeDescription() == null || recipe.getRecipeDescription().isEmpty())
                        || recipe.getUserId() == null
                        || (recipe.getIngredients() == null || recipe.getIngredients().isEmpty())
        ){
            logger.error("Recipe is missing some content and cannot be created!");
            throw new RecipeEmptyException("Recipe Is Missing Some Content!");
        }

        if (recipeRepo.existsByRecipeName(recipe.getRecipeName())) {
            logger.error("Recipe already exists with name of: {}.", recipe.getRecipeName());
            throw new RecipeExistsException("Recipe Already Exists With Name Of: " + recipe.getRecipeName());
        }

        if (recipe.getImageId() == null || recipe.getImageId().isEmpty()) {
            logger.info("No imageId for created Recipe, setting to empty String.");
            recipe.setImageId(noImageId);
        }

        return recipeRepo.insert(
                new Recipe(
                        recipe.getUserId(),
                        recipe.getRecipeName(),
                        recipe.getRecipeDescription(),
                        recipe.getIngredients(),
                        recipe.getSteps(),
                        recipe.getImageId(),
                        0
                )
        );
    }

    public Recipe updateRecipe(ObjectId id, Recipe recipe) {

        if (
                (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty())
                        || ( recipe.getRecipeDescription() == null || recipe.getRecipeDescription().isEmpty())
                        || recipe.getUserId() == null
                        || (recipe.getIngredients() == null || recipe.getIngredients().isEmpty())
                        || (recipe.getSteps() == null || recipe.getSteps().isEmpty())
        ){
            logger.error("Recipe with Id of \"{}\" is missing some content and cannot be updated!", id);
            throw new RecipeEmptyException("Recipe Is Missing Some Content!");
        }

        Recipe foundRecipe = recipeRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Recipe does not exists with Id of \"{}\" and cannot be updated!", id);
                    return new RecipeNotFoundException("Recipe Does Not Exists With Id Of: " + id);
                });

        foundRecipe.setRecipeName(recipe.getRecipeName());
        foundRecipe.setRecipeDescription(recipe.getRecipeDescription());
        foundRecipe.setIngredients(recipe.getIngredients());
        foundRecipe.setSteps(recipe.getSteps());
        foundRecipe.setImageId(recipe.getImageId());

        recipeRepo.save(foundRecipe);
        return foundRecipe;
    }

    public Recipe deleteRecipeById(ObjectId id) {
        Recipe foundRecipe = recipeRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Recipe does not exists with Id of \"{}\" and cannot be deleted!", id);
                    return new RecipeNotFoundException("Recipe Does Not Exists With Id Of: " + id);
                });

        recipeRepo.deleteById(id);
        return  foundRecipe;
    }
}
