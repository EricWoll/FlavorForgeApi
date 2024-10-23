package com.flavor.forge.Service;

import com.flavor.forge.Repo.RecipeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepo recipeRepo;
}
