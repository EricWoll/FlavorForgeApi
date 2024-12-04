package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.RecipeEmptyException;
import com.flavor.forge.Exception.CustomExceptions.RecipeNotFoundException;
import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.FollowedCreator;
import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.Response.RecipeWithUser;
import com.flavor.forge.Model.Response.RecipeWithUserLoggedIn;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.FollowedCreatorRepo;
import com.flavor.forge.Repo.RecipeRepo;
import com.flavor.forge.Repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;


import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private Logger logger = LoggerFactory.getLogger(RecipeService.class);

    @Autowired
    private RecipeRepo recipeRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowedCreatorRepo followedCreatorRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${forge.app.noImage}")
    private String noImageId;

    public RecipeWithUser findRecipeWithUser(String recipeId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("recipeId").is(recipeId));
        LookupOperation userLookupOperation = Aggregation.lookup(
                "user",
                "creatorId",
                "userId",
                "creator"
        );

        // Unwind the userDetails array (created by $lookup)
        UnwindOperation unwindCreatorOperation = Aggregation.unwind("creator", true); // Preserve null/empty results

        ProjectionOperation projectionOperation = Aggregation.project()
                .andInclude(
                        "recipeId",
                        "recipeName",
                        "recipeDescription",
                        "ingredients",
                        "steps",
                        "imageId",
                        "likesCount"
                )
                .and("creator.userId").as("creatorId")
                .and("creator.username").as("creator");

        LimitOperation limitOperation = Aggregation.limit(1);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                userLookupOperation,
                unwindCreatorOperation,
                projectionOperation,
                limitOperation
        );

        List<RecipeWithUser> result =  mongoTemplate.aggregate(
                aggregation,
                "recipe",
                RecipeWithUser.class
        ).getMappedResults();
        return result.isEmpty() ? null : result.getFirst();
    }

    public RecipeWithUserLoggedIn findRecipeWithUserLoggedIn(String recipeId, String currentUserId) {
        // Step 1: Fetch the recipe by recipeId
        Recipe recipe = recipeRepo.findByRecipeId(recipeId).orElseThrow(
                () -> new RecipeNotFoundException("Recipe not found")
        );

        // Step 2: Fetch the user who created the recipe
        String creatorId = recipe.getCreatorId();
        User creator = userRepo.findByUserId(creatorId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );

        // Step 3: Check if currentUserId is following the creator
        boolean isFollowing = isFollowing(currentUserId, creatorId);

        // Step 4: Prepare RecipeWithUserLoggedIn response object
        return RecipeWithUserLoggedIn.builder()
                .creatorId(creator.getUserId())
                .creatorUsername(creator.getUsername())
                .recipeId(recipe.getRecipeId())
                .recipeName(recipe.getRecipeName())
                .recipeDescription(recipe.getRecipeDescription())
                .ingredients(recipe.getIngredients())
                .steps(recipe.getSteps())
                .imageId(recipe.getImageId())
                .likesCount(recipe.getLikesCount())
                .isFollowing(isFollowing)
                .build();
    }


    public List<Recipe> findAllByUserId(String creatorId) {
        return recipeRepo.findAllByCreatorId(creatorId);
    }

    public Recipe createRecipe(Recipe recipe) {

        if (
                (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty())
                        || ( recipe.getRecipeDescription() == null || recipe.getRecipeDescription().isEmpty())
                        || recipe.getCreatorId() == null
                        || ( recipe.getIngredients() == null || recipe.getIngredients().isEmpty() )
        ){
            logger.error("Recipe is missing some content and cannot be created!");
            throw new RecipeEmptyException("Recipe Is Missing Some Content!");
        }

        if (recipe.getImageId() == null || recipe.getImageId().isEmpty()) {
            recipe.setImageId(noImageId);
        }

        return recipeRepo.insert(
                new Recipe(
                        recipe.getCreatorId(),
                        recipe.getRecipeName(),
                        recipe.getRecipeDescription(),
                        recipe.getIngredients(),
                        recipe.getSteps(),
                        recipe.getImageId(),
                        0
                )
        );
    }

    public Recipe updateRecipe(String id, Recipe recipe) {
        if (
                (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty())
                        || ( recipe.getRecipeDescription() == null || recipe.getRecipeDescription().isEmpty())
                        || recipe.getCreatorId() == null
                        || (recipe.getIngredients() == null || recipe.getIngredients().isEmpty())
                        || (recipe.getSteps() == null || recipe.getSteps().isEmpty())
        ){

            logger.error("Recipe with Id of \"{}\" is missing some content and cannot be updated!", id);
            throw new RecipeEmptyException("Recipe Is Missing Some Content!");
        }

        Recipe foundRecipe = recipeRepo.findByRecipeId(id)
                .orElseThrow(() -> {
                    logger.error("Recipe does not exists with Id of \"{}\" and cannot be updated!", id);
                    return new RecipeNotFoundException("Recipe Does Not Exists With Id Of: " + id);
                });

        foundRecipe.setImageId(recipe.getImageId());
        foundRecipe.setRecipeName(recipe.getRecipeName());
        foundRecipe.setRecipeDescription(recipe.getRecipeDescription());
        foundRecipe.setIngredients(recipe.getIngredients());
        foundRecipe.setSteps(recipe.getSteps());

        recipeRepo.save(foundRecipe);
        return foundRecipe;
    }

    public Recipe deleteRecipeById(String id) {
        Recipe foundRecipe = recipeRepo.findByRecipeId(id)
                .orElseThrow(() -> {
                    logger.error("Recipe does not exists with Id of \"{}\" and cannot be deleted!", id);
                    return new RecipeNotFoundException("Recipe Does Not Exists With Id Of: " + id);
                });

        recipeRepo.deleteByRecipeId(id);
        return  foundRecipe;
    }

    public boolean isFollowing(String currentUserId, String creatorId) {
        Optional<FollowedCreator> follow = followedCreatorRepo.findByUserIdAndCreatorId(currentUserId, creatorId);
        return follow.isPresent();
    }
}
