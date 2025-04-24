package com.flavor.forge.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavor.forge.Exception.CustomExceptions.RecipeEmptyException;
import com.flavor.forge.Exception.CustomExceptions.RecipeNotFoundException;
import com.flavor.forge.Model.DTO.RecipeWithCreatorDTO;
import com.flavor.forge.Model.Ingredient;
import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Repo.RecipeRepo;
import com.flavor.forge.Repo.UserRepo;
import com.flavor.forge.Security.Jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecipeService {

    private Logger logger = LoggerFactory.getLogger(RecipeService.class);

    @Autowired
    private RecipeRepo recipeRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;

    @Value("${SEARCH_LIMIT}")
    private short searchLimit;

    @Value("${forge.app.noImage}")
    private String noImageId;

    public RecipeWithCreatorDTO findByRecipeId(UUID recipeId) {
        return recipeRepo.findByRecipeIdWithCreator(recipeId).orElseThrow(
                () -> new RecipeNotFoundException("No recipe with the Id of: " + recipeId + " was found")
        );
    }

    public List<RecipeWithCreatorDTO> defaultSearch(int listOffset, UUID creatorId) {
        List<Object[]> recipeList = recipeRepo.findRandomRecipes(searchLimit, listOffset, creatorId);

        if (recipeList.isEmpty()) {
            logger.error("No Recipes Were Found in the Database!");
            throw new RecipeNotFoundException("No Recipes were found!");
        }

        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }


    public List<RecipeWithCreatorDTO> searchWithSearchWordAndIngredients(String searchWord, List<Ingredient> ingredients, UUID creatorId, int listOffset) {
        String filterIngredientsJson = null;

        if (ingredients != null && !ingredients.isEmpty()) {
            // Extract only ingredient names into JSON array of objects like [{"ingredientName": "Lemon Juice"}]
            List<Map<String, String>> ingredientNameObjects = ingredients.stream()
                    .map(ing -> Map.of("ingredientName", ing.getIngredientName()))
                    .toList();

            try {
                ObjectMapper mapper = new ObjectMapper();
                filterIngredientsJson = mapper.writeValueAsString(ingredientNameObjects);
            } catch (JsonProcessingException e) {
                logger.error("Error converting ingredients to JSON", e);
                throw new RuntimeException("Error preparing ingredient filter");
            }
        }

        List<Object[]> recipeList = recipeRepo.findRecipesWithSearchWordAndIngredients(searchWord, filterIngredientsJson, creatorId, searchLimit, listOffset);

        if (recipeList.isEmpty()) {
            logger.error("No Recipes with the title: {} were found!", searchWord);
            throw new RecipeNotFoundException("No Recipes with the title: " + searchWord +" were found!");
        }
        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }


    public List<RecipeWithCreatorDTO> searchLikedRecipesDefault(UUID userId, int listOffset, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);
        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, userId);

        List<Object[]> recipeList = recipeRepo.findLikedRecipesRandom(userId, searchLimit, listOffset);

        if (recipeList.isEmpty()) {
            logger.error("No Recipes were found!");
            throw new RecipeNotFoundException("No Recipes were found!");
        }

        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }

    public List<RecipeWithCreatorDTO> searchLikedRecipesWithSearchWordAndFilters(
            UUID userId,
            String searchWord,
            List<Ingredient> ingredients,
            int listOffset,
            String accessToken
    ) {
        jwtService.validateAccessTokenCredentials(accessToken);

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, userId);

        List<String> ingredientNames = null;
        if (ingredients != null && !ingredients.isEmpty()) {
            ingredientNames = ingredients.stream()
                    .map(Ingredient::getIngredientName)
                    .toList();
        };

        List<Object[]> recipeList = recipeRepo.findLikedRecipesWithSearchWordAndFilters(
                userId, searchWord, ingredientNames, searchLimit, listOffset
        );

        if (recipeList.isEmpty()) {
            logger.error("No Liked Recipes were found!");
            throw new RecipeNotFoundException("No Liked Recipes were found!");
        }

        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }

    public Recipe createRecipe(Recipe recipe, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);
        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, recipe.getCreatorId());

        if (
                (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty())
                        || (recipe.getRecipeDescription() == null || recipe.getRecipeDescription().isEmpty())
                        || recipe.getCreatorId() == null
                        || (recipe.getIngredients() == null || recipe.getIngredients().isEmpty())
        ) {
            logger.error("Recipe is missing some content and cannot be created!");
            throw new RecipeEmptyException("Recipe Is Missing Some Content!");
        }

        if (recipe.getImageId() == null || recipe.getImageId().isEmpty()) {
            recipe.setImageId(noImageId);
        }

        return recipeRepo.save(recipe);
    }

    public Recipe updateRecipe(UUID recipeId, Recipe recipe, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        if (
                (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty())
                        || ( recipe.getRecipeDescription() == null || recipe.getRecipeDescription().isEmpty())
                        || recipe.getCreatorId() == null
                        || (recipe.getIngredients() == null || recipe.getIngredients().isEmpty())
                        || (recipe.getSteps() == null || recipe.getSteps().isEmpty())
        ){

            logger.error("Recipe with Id of \"{}\" is missing some content and cannot be updated!", recipeId);
            throw new RecipeEmptyException("Recipe Is Missing Some Content!");
        }

        Recipe foundRecipe = recipeRepo.findByRecipeId(recipeId)
                .orElseThrow(() -> {
                    logger.error("Recipe does not exists with Id of \"{}\" and cannot be updated!", recipeId);
                    return new RecipeNotFoundException("Recipe Does Not Exists With Id Of: " + recipeId);
                });

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, foundRecipe.getCreatorId());

        foundRecipe.setImageId(recipe.getImageId());
        foundRecipe.setRecipeName(recipe.getRecipeName());
        foundRecipe.setRecipeDescription(recipe.getRecipeDescription());
        foundRecipe.setIngredients(recipe.getIngredients());
        foundRecipe.setSteps(recipe.getSteps());

        recipeRepo.save(foundRecipe);
        return foundRecipe;
    }

    public Recipe deleteRecipeById(UUID recipeId, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        Recipe foundRecipe = recipeRepo.findByRecipeId(recipeId)
                .orElseThrow(() -> {
                    logger.error("Recipe does not exists with Id of \"{}\" and cannot be deleted!", recipeId);
                    return new RecipeNotFoundException("Recipe Does Not Exists With Id Of: " + recipeId);
                });

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, foundRecipe.getCreatorId());

        recipeRepo.deleteByRecipeId(foundRecipe.getRecipeId());
        return  foundRecipe;
    }

    // Mapper Utility for SQL Queries
    private RecipeWithCreatorDTO sqlQueryObjectMapToRecipeWithCreatorDTO(Object[] sqlQueryResult) {
        UUID recipeId = (UUID) sqlQueryResult[0];
        UUID recipeCreatorId = (UUID) sqlQueryResult[1];
        String creatorImageId = (String) sqlQueryResult[2];
        String creatorUsername = (String) sqlQueryResult[3];
        String recipeName = (String) sqlQueryResult[4];
        String recipeImageId = (String) sqlQueryResult[5]; // Also "none" sometimes
        String recipeDescription = (String) sqlQueryResult[6];
        String foundIngredientsJson = (String) sqlQueryResult[7];
        List<String> steps = Arrays.asList((String[]) sqlQueryResult[8]);
        int likesCount = (Integer) sqlQueryResult[9];
        int viewsCount = (Integer) sqlQueryResult[10];
        boolean isLiked = sqlQueryResult.length > 11 ? (Boolean) sqlQueryResult[11] : false;

        return new RecipeWithCreatorDTO(
                recipeId, recipeCreatorId, creatorImageId, creatorUsername,
                recipeName, recipeImageId, recipeDescription,
                foundIngredientsJson, steps, likesCount, viewsCount, isLiked
        );
    }
}
