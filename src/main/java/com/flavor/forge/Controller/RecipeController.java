package com.flavor.forge.Controller;

import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.Response.RecipeWithUser;
import com.flavor.forge.Model.Response.RecipeWithUserLoggedIn;
import com.flavor.forge.Service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping("/{recipe_id}")
    public ResponseEntity<RecipeWithUser> findSingleRecipe(
            @PathVariable String recipe_id
    ) {
        return new ResponseEntity<RecipeWithUser>(
                recipeService.findRecipeWithUser(recipe_id),
                HttpStatus.OK
        );
    }

    @GetMapping("/followed/{recipe_id}")
    public ResponseEntity<RecipeWithUserLoggedIn> findSingleRecipeLoggedIn(
            @PathVariable String recipe_id,
            @RequestParam("user_id") String user_id
    ) {
        return new ResponseEntity<RecipeWithUserLoggedIn>(
                recipeService.findRecipeWithUserLoggedIn(recipe_id, user_id),
                HttpStatus.OK
        );
    }

    @GetMapping("users/{user_id}")
    public ResponseEntity<List<Recipe>> findAllRecipesByUserId(@PathVariable String user_id) {
        return new ResponseEntity<List<Recipe>>(
                recipeService.findAllByUserId(user_id),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe payload) {
        return new ResponseEntity<Recipe>(
                recipeService.createRecipe(payload),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{recipe_id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable String recipe_id,
            @RequestBody Recipe payload
    ) {
        return new ResponseEntity<Recipe>(
                recipeService.updateRecipe(recipe_id, payload),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{recipe_id}")
    public ResponseEntity<Recipe> deleteRecipe(@PathVariable String recipe_id) {
        return new ResponseEntity<Recipe>(
                recipeService.deleteRecipeById(recipe_id),
                HttpStatus.NO_CONTENT
        );
    }
}
