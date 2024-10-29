package com.flavor.forge.Controller;

import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Service.RecipeService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public ResponseEntity<Recipe> findSingleRecipe(@PathVariable ObjectId recipe_id) {
        return new ResponseEntity<Recipe>(
                recipeService.findOneById(recipe_id),
                HttpStatus.OK
        );
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<List<Recipe>> findAllRecipesByUserId(@PathVariable ObjectId user_id) {
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
            @PathVariable ObjectId recipe_id,
            @RequestBody Recipe payload
    ) {
        return new ResponseEntity<Recipe>(
                recipeService.updateRecipe(recipe_id, payload),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{recipe_id}")
    public ResponseEntity<Recipe> deleteRecipe(@PathVariable ObjectId recipe_id) {
        return new ResponseEntity<Recipe>(
                recipeService.deleteRecipeById(recipe_id),
                HttpStatus.NO_CONTENT
        );
    }
}
