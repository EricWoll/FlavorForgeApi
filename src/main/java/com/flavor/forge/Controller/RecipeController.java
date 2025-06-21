package com.flavor.forge.Controller;

import com.flavor.forge.Model.DTO.RecipeWithCreatorDTO;
import com.flavor.forge.Model.Ingredient;
import com.flavor.forge.Model.LikedRecipe;
import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Service.RecipeService;
import com.flavor.forge.Utils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("api/v2/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    private Utils utils;

    @GetMapping("/search")
    public ResponseEntity<List<RecipeWithCreatorDTO>> searchRecipes(
            @RequestParam(value = "ingredients", required = false) List<Ingredient> ingredients,
            @RequestParam(value = "search_string", required = false) String searchString,
            @RequestParam(value = "creator_id", required = false) String creatorId,
            @RequestParam(value = "user_id", required = false) String userId,
            @RequestParam(value = "listOffset", defaultValue = "0") int listOffset
    ) {

        boolean hasIngredients = ingredients != null && !ingredients.isEmpty();
        boolean hasSearchString = searchString != null && !searchString.isEmpty();

        List<RecipeWithCreatorDTO> results;

        if (hasIngredients || hasSearchString) {
            results = recipeService.searchWithSearchWordAndIngredients(searchString, ingredients, creatorId, userId, listOffset);
        } else {
            results = recipeService.defaultSearch(creatorId, userId, listOffset);
        }

        return ResponseEntity.ok(results);
    }


    @GetMapping("/search/{recipe_id}")
    public ResponseEntity<RecipeWithCreatorDTO> findSingleRecipe(
            @PathVariable(value = "recipe_id") UUID recipeId,
            @RequestParam(value = "user_id", required = false) String userId
    ) {
        if (!Utils.validateUUIDs(recipeId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<RecipeWithCreatorDTO>(
                recipeService.findByRecipeId(recipeId, userId),
                HttpStatus.OK
        );
    }


    @GetMapping("/liked/search/{user_id}")
    public ResponseEntity<List<RecipeWithCreatorDTO>> searchLikedRecipes (
            @PathVariable(value = "user_id") String userId,
            @RequestParam(value = "filters", required = false) List<Ingredient> filters,
            @RequestParam(value = "search_string", required = false) String searchString,
            @RequestParam(value = "listOffset", defaultValue = "0") int listOffset,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        boolean hasFilters = filters != null && !filters.isEmpty();
        boolean hasSearchString = searchString != null && !searchString.isEmpty();

        List<RecipeWithCreatorDTO> results;

        if (hasFilters || hasSearchString) {
            results = recipeService.searchLikedRecipesWithSearchWordAndFilters(userId, searchString, filters, listOffset, accessToken);
        } else {
            results = recipeService.searchLikedRecipesDefault(userId, listOffset, accessToken);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/create")
    public ResponseEntity<Recipe> createRecipe(
            @Valid @RequestBody Recipe recipe,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        return new ResponseEntity<Recipe>(
                recipeService.createRecipe(recipe, accessToken),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/liked/add/{user_id}")
    public ResponseEntity<LikedRecipe> addLikedRecipe(
            @PathVariable(value = "user_id") String userId,
            @RequestParam(value = "recipe_id") UUID recipeId,
            @RequestHeader(name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(recipeId)) {
            return ResponseEntity.badRequest().build();
        }
        return  new ResponseEntity<LikedRecipe>(
                recipeService.addLikedRecipe(userId, recipeId, accessToken),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/update/{recipe_id}")
    public ResponseEntity<Recipe> updateRecipe(
            @Valid
            @PathVariable(value = "recipe_id") UUID recipeId,
            @RequestBody Recipe recipe,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(recipeId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<Recipe>(
                recipeService.updateRecipe(recipeId, recipe, accessToken),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete/{recipe_id}")
    public ResponseEntity<Recipe> deleteRecipe(
            @PathVariable(value = "recipe_id") UUID recipeId,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(recipeId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<Recipe>(
                recipeService.deleteRecipeById(recipeId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }

    @DeleteMapping("/liked/delete/{user_id}")
    public ResponseEntity<LikedRecipe> removeLikedRecipe(
            @PathVariable(value = "user_id") String userId,
            @RequestParam(value = "recipe_id") UUID recipeId,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(recipeId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<LikedRecipe>(
                recipeService.removeLikedRecipe(userId, recipeId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }
}
