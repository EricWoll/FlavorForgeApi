package com.flavor.forge.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "recipe_id", updatable = false, nullable = false)
    private UUID recipeId;

    @NotNull
    @Column(name = "creator_id")
    private UUID creatorId;

    @NotNull
    @NotEmpty
    @Size(min = 6, message = "Recipe name must have at least 6 characters!")
    @Column(name = "recipe_name")
    private String recipeName;

    @NotNull
    @NotEmpty
    @Size(min = 6, message = "Recipe description must have at least 6 characters!")
    @Column(name = "recipe_description")
    private String recipeDescription;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)  // This will work for using jsonb column type with hibernate-types
    @Column(name = "ingredients", columnDefinition = "jsonb")
    private List<Ingredient> ingredients;

    @NotNull
    @Column(name = "steps", columnDefinition = "text[]")
    private List<String> steps;

    @Column(name = "image_id")
    private String imageId;

    @NotNull
    @Min(0)
    @Column(name = "likes_count")
    private int likesCount;

    @NotNull
    @Min(0)
    @Column(name = "views_count")
    private int viewsCount;

    @NotNull
    @Column(name = "is_private")
    private boolean isPrivate;

    public Recipe(
            UUID creatorId,
            String recipeName,
            String recipeDescription,
            List<Ingredient> ingredients,
            List<String> steps,
            String imageId,
            int likesCount,
            int viewsCount,
            boolean isPrivate
    ) {
        this.creatorId = creatorId;
        this.recipeName = recipeName;
        this.recipeDescription = recipeDescription;
        this.ingredients = ingredients;
        this.steps = steps;
        this.imageId = imageId;
        this.likesCount = likesCount;
        this.viewsCount = viewsCount;
        this.isPrivate = isPrivate;
    }

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
