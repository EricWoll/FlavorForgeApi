package com.flavor.forge.Service;

import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.Request.ListSearchRequest;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.RecipeRepo;
import com.flavor.forge.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private RecipeRepo recipeRepo;

    @Autowired
    private UserRepo userRepo;

    public List<Recipe> defaultSearchInRecipes(int pageAmount) {
        return recipeRepo.defaultSearchInRecipes(pageAmount);
    }

    public List<Recipe> searchByStringInRecipe(String searchString, ListSearchRequest ingredients) {
        if (ingredients.getSearchList().isEmpty()) {
            return recipeRepo.findAllByRecipeName(searchString);
        }
            return recipeRepo.findAllByRecipeNameAndIngredients(searchString, ingredients.getSearchList());
    }

    public List<User> searchByStringInUser(String searchString) {
        return userRepo.findAllByUsername(searchString);
    }
}
