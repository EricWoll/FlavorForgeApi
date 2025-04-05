package com.flavor.forge.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "liked_recipe")
public class LikedRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID likedId;
    private UUID userId;
    private UUID recipeId;

    public LikedRecipe(UUID userId, UUID recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
    }
}
