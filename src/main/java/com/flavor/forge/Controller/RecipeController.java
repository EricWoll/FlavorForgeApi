package com.flavor.forge.Controller;

import com.flavor.forge.Model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("api/v2/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping("/")
    public ResponseEntity<List<Recipe>> searchRecipes(
            @RequestParam(value = "filters", required = false) List<String> filters,
            @RequestParam(value = "search_string", required = false) String searchString
    ) {
        boolean hasFilters = filters != null && !filters.isEmpty();
        boolean hasSearchString = searchString != null && !searchString.isEmpty();

        List<Recipe> results;

        if (hasFilters && hasSearchString) {
            results = recipeService.searchWithSearchWordAndFilter(searchString, filters);
        } else if (hasFilters) {
            results = recipeService.searchWithFilters(filters);
        } else if (hasSearchString) {
            results = recipeService.searchWithSearchWord(searchString);
        } else {
            results = recipeService.defaultSearch();
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/")
    public ResponseEntity<Recipe> createRecipe(
            @RequestParam(value = "recipe") Recipe recipe,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<Recipe>(
                recipeService.createRecipe(recipe, accessToken),
                HttpStatus.OK
        );
    }

    @GetMapping("/{recipe_id}")
    public ResponseEntity<Optional<Recipe>> findSingleRecipe(
            @PathVariable(value = "recipe_id") UUID recipeId
    ) {
        return new ResponseEntity<Optional<Recipe>>(
                recipeService.findSingleRecipe(recipeId),
                HttpStatus.OK
        );
    }

    @PutMapping("/{recipe_id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable(value = "recipe_id") UUID recipeId,
            @RequestParam(value = "recipe") Recipe recipe,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<Recipe>(
                recipeService.updateRecipe(recipeId, recipe, accessToken),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{recipe_id}")
    public ResponseEntity<Recipe> deleteRecipe(
            @PathVariable(value = "recipe_id") UUID recipeId,
            @RequestParam("access_token") String accessToken
    ) {
        return new ResponseEntity<Recipe>(
                recipeService.deleteRecipe(recipeId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }

    @GetMapping("/liked/{user_id}")
    public ResponseEntity<List<Recipe>> searchLikedRecipes (
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "access_token") String accessToken,
            @RequestParam(value = "filters", required = false) List<String> filters,
            @RequestParam(value = "search_string", required = false) String searchString
    ) {
        boolean hasFilters = filters != null && !filters.isEmpty();
        boolean hasSearchString = searchString != null && !searchString.isEmpty();

        List<Recipe> results;

        if (hasFilters && hasSearchString) {
            results = recipeService.searchLikedWithSearchWordAndFilter(searchString, filters, userId, accessToken);
        } else if (hasFilters) {
            results = recipeService.searchLikedWithFilters(filters, userId, accessToken);
        } else if (hasSearchString) {
            results = recipeService.searchLikedWithSearchWord(searchString, userId, accessToken);
        } else {
            results = recipeService.defaultSearchLiked(userId, accessToken);
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/creator/{user_id}")
    public ResponseEntity<List<Recipe>> searchCreatorRecipes (
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "access_token") String accessToken,
            @RequestParam(value = "filters", required = false) List<String> filters,
            @RequestParam(value = "search_string", required = false) String searchString
    ) {
        boolean hasFilters = filters != null && !filters.isEmpty();
        boolean hasSearchString = searchString != null && !searchString.isEmpty();

        List<Recipe> results;

        if (hasFilters && hasSearchString) {
            results = recipeService.searchCreatorWithSearchWordAndFilter(searchString, filters, userId, accessToken);
        } else if (hasFilters) {
            results = recipeService.searchCreatorWithFilters(filters, userId, accessToken);
        } else if (hasSearchString) {
            results = recipeService.searchCreatorWithSearchWord(searchString, userId, accessToken);
        } else {
            results = recipeService.defaultSearchCreator(userId, accessToken);
        }

        return ResponseEntity.ok(results);
    }
}
