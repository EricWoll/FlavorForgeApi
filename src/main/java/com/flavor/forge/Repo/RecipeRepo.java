package com.flavor.forge.Repo;

import com.flavor.forge.Model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe, UUID> {

    Optional<Recipe> findByRecipeId(UUID recipeId);

    boolean existsByRecipeName(String name);
    boolean existsByRecipeId(UUID recipeId);

    void deleteByRecipeId(UUID recipeId);
}
