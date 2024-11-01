package com.flavor.forge.Service;

import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.RecipeRepo;
import com.flavor.forge.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private RecipeRepo recipeRepo;

    @Autowired
    private UserRepo userRepo;

    public List<Recipe> searchByStringInRecipe(String searchString, List<String> ingredients) {
        return recipeRepo.findAllByRecipeName(searchString, ingredients);
    }

    public List<User> searchByStringInUser(String searchString) {
        return userRepo.findAllByUsername(searchString);
    }
}
