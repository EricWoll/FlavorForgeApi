package com.flavor.forge.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recipeId;
    private UUID creatorId;
    private String recipeName;
    private String recipeDescription;

    @ElementCollection
    @CollectionTable(name = "recipeIngredients", joinColumns = @JoinColumn(name = "recipeId"))
    @Column(name = "ingredient")
    private List<Ingredient> ingredients;

    @ElementCollection
    @CollectionTable(name = "recipeSteps", joinColumns = @JoinColumn(name = "recipeId"))
    @Column(name = "step")
    private List<String> steps;

    private String imageId;
    private int likesCount;
    private int viewsCount;
    private boolean isPrivate;

    public void addLike() {
        this.likesCount++;
    }
    public void removeLike() {
        this.likesCount--;
    }

    public void addView() {
        this.viewsCount++;
    }
    public void removeView() {
        this.viewsCount--;
    }
}
