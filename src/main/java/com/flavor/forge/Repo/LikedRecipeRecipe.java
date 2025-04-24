package com.flavor.forge.Repo;

import com.flavor.forge.Model.LikedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikedRecipeRecipe extends JpaRepository<LikedRecipe, UUID> {

    Optional<LikedRecipe> findByRecipe_recipeId(UUID recipeId);

    List<LikedRecipe> findAllByUser_userId(UUID userId);
}
