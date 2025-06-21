package com.flavor.forge.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavor.forge.Exception.CustomExceptions.RecipeEmptyException;
import com.flavor.forge.Exception.CustomExceptions.RecipeExistsException;
import com.flavor.forge.Exception.CustomExceptions.RecipeNotFoundException;
import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.DTO.RecipeWithCreatorDTO;
import com.flavor.forge.Model.Ingredient;
import com.flavor.forge.Model.LikedRecipe;
import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.LikedRecipeRepo;
import com.flavor.forge.Repo.RecipeRepo;
import com.flavor.forge.Repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private LikedRecipeRepo likedRecipeRepo;

    @Value("${SEARCH_LIMIT}")
    private short searchLimit;

    @Value("${forge.app.noImage}")
    private String noImageId;

    public RecipeWithCreatorDTO findByRecipeId(UUID recipeId, String userId) {
        logger.info("UserId: {}", userId);
        Object[] recipe = (Object[]) recipeRepo.findByRecipeIdWithCreator(recipeId, userId).orElseThrow(
                () -> new RecipeNotFoundException("No recipe with the Id of: " + recipeId + " was found")
        );
        return sqlQueryObjectMapToRecipeWithCreatorDTO(recipe);
    }

    public List<RecipeWithCreatorDTO> defaultSearch(String creatorId, String userId, int listOffset) {
        List<Object[]> recipeList = recipeRepo.findRandomRecipes(creatorId, userId, searchLimit, listOffset);

        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }


    public List<RecipeWithCreatorDTO> searchWithSearchWordAndIngredients(String searchWord, List<Ingredient> ingredients, String creatorId, String userId, int listOffset) {
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

        List<Object[]> recipeList = recipeRepo.findRecipesWithSearchWordAndIngredients(searchWord, filterIngredientsJson, creatorId, userId, searchLimit, listOffset);

        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }


    public List<RecipeWithCreatorDTO> searchLikedRecipesDefault(String userId, int listOffset, String accessToken) {
        System.out.println("Searching liked recipes random");
        System.out.println("UserId: " + userId);

        List<Object[]> recipeList = likedRecipeRepo.findLikedRecipesRandom(userId, searchLimit, listOffset);
        System.out.println(recipeList);

        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }

    public List<RecipeWithCreatorDTO> searchLikedRecipesWithSearchWordAndFilters(
            String userId,
            String searchWord,
            List<Ingredient> ingredients,
            int listOffset,
            String accessToken
    ) {

        List<String> ingredientNames = null;
        if (ingredients != null && !ingredients.isEmpty()) {
            ingredientNames = ingredients.stream()
                    .map(Ingredient::getIngredientName)
                    .toList();
        };

        List<Object[]> recipeList = likedRecipeRepo.findLikedRecipesWithSearchWordAndFilters(
                userId, searchWord, ingredientNames, searchLimit, listOffset
        );

        return recipeList.stream().map(this::sqlQueryObjectMapToRecipeWithCreatorDTO).toList();
    }

    public Recipe createRecipe(Recipe recipe, String accessToken) {

        if (
                (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty())
                        || (recipe.getRecipeDescription() == null || recipe.getRecipeDescription().isEmpty())
                        || recipe.getCreatorId() == null
                        || (recipe.getIngredients() == null || recipe.getIngredients().isEmpty())
        ) {
            logger.error("Recipe is missing some content and cannot be created!");
            throw new RecipeEmptyException("Recipe Is Missing Some Content!");
        }
        logger.info("Recipe Image Id: {}", recipe.getImageId());

        if (recipe.getImageId() == null || recipe.getImageId().isEmpty()) {
            recipe.setImageId(noImageId);
        }

        return recipeRepo.save(recipe);
    }

    public Recipe updateRecipe(UUID recipeId, Recipe recipe, String accessToken) {

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

        foundRecipe.setImageId(recipe.getImageId());
        foundRecipe.setRecipeName(recipe.getRecipeName());
        foundRecipe.setRecipeDescription(recipe.getRecipeDescription());
        foundRecipe.setIngredients(recipe.getIngredients());
        foundRecipe.setSteps(recipe.getSteps());

        recipeRepo.save(foundRecipe);
        return foundRecipe;
    }

    @Transactional
    public Recipe deleteRecipeById(UUID recipeId, String accessToken) {

        Recipe foundRecipe = recipeRepo.findByRecipeId(recipeId)
                .orElseThrow(() -> {
                    logger.error("Recipe does not exists with Id of \"{}\" and cannot be deleted!", recipeId);
                    return new RecipeNotFoundException("Recipe Does Not Exists With Id Of: " + recipeId);
                });

        recipeRepo.deleteByRecipeId(foundRecipe.getRecipeId());
        return  foundRecipe;
    }

    public LikedRecipe addLikedRecipe(String userId, UUID recipeId, String accessToken) {

        if (likedRecipeRepo.existsByUser_UserIdAndRecipe_RecipeId(userId, recipeId)) {
            throw new RecipeExistsException("Recipe has already been liked by user with id of: " + userId);
        };

        Recipe foundRecipe = recipeRepo.findByRecipeId(recipeId).orElseThrow(() -> new RecipeNotFoundException("Recipe with Id: " + recipeId + " does not exist!"));
        User foundUser = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User with Id: " + userId + " does not exist!"));

        return likedRecipeRepo.save(new LikedRecipe(foundUser, foundRecipe));
    }


    public LikedRecipe removeLikedRecipe(String userId, UUID recipeId, String accessToken) {

        LikedRecipe foundLikedRecipe = likedRecipeRepo.findLikedRecipeByUserIdAndRecipeId(userId, recipeId).orElseThrow(
                () -> new RecipeExistsException("Recipe has NOT been liked by user with id of: " + userId)
        );

        likedRecipeRepo.deleteById(foundLikedRecipe.getLikedId());
        return foundLikedRecipe;
    }

    // Mapper Utility for SQL Queries to Recipes
    private RecipeWithCreatorDTO sqlQueryObjectMapToRecipeWithCreatorDTO(Object[] sqlQueryResult) {
        UUID recipeId = (UUID) sqlQueryResult[0];
        String recipeCreatorId = (String) sqlQueryResult[1];
        String creatorImageId = (String) sqlQueryResult[2];
        String creatorUsername = (String) sqlQueryResult[3];
        String recipeName = (String) sqlQueryResult[4];
        String recipeImageId = (String) sqlQueryResult[5]; // Also "none" sometimes
        String recipeDescription = (String) sqlQueryResult[6];
        String foundIngredientsJson = (String) sqlQueryResult[7];
        List<String> steps = Arrays.asList((String[]) sqlQueryResult[8]);
        int likesCount = (Integer) sqlQueryResult[9];
        int viewsCount = (Integer) sqlQueryResult[10];

        Object isLikedRaw = sqlQueryResult[11];
        boolean isLiked = false;

        if (isLikedRaw instanceof Boolean) {
            isLiked = (Boolean) isLikedRaw;
        } else if (isLikedRaw instanceof Number) {
            isLiked = ((Number) isLikedRaw).intValue() != 0;
        } else if (isLikedRaw instanceof String) {
            isLiked = Boolean.parseBoolean((String) isLikedRaw);
        }

        return new RecipeWithCreatorDTO(
                recipeId, recipeCreatorId, creatorImageId, creatorUsername,
                recipeName, recipeImageId, recipeDescription,
                foundIngredientsJson, steps, likesCount, viewsCount, isLiked
        );
    }
}
