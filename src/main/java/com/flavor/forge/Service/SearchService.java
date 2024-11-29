package com.flavor.forge.Service;

import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.RecipeRepo;
import com.flavor.forge.Repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchService {

    private Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private RecipeRepo recipeRepo;

    @Autowired
    private UserRepo userRepo;

    public List<Recipe> defaultSearchInRecipes(int pageAmount) {
        return recipeRepo.defaultSearchInRecipes(pageAmount);
    }

    public List<Recipe> searchByStringInRecipe(String searchString) {
        return recipeRepo.findAllByRecipeName(searchString);
    }

    public List<Recipe> searchRecipeByUserIdAndSearchString(String userId, String searchString) {
        return recipeRepo.findAllByUserIdAndSearchString(userId, searchString);
    }

    public List<User> searchByStringInUser(String searchString) {
        return userRepo.findAllByUsername(searchString);
    }
}
