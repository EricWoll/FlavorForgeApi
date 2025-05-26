package com.flavor.forge.Repo;

import com.flavor.forge.Model.LikedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikedRecipeRepo extends JpaRepository<LikedRecipe, UUID> {

    @Query(value = """
            SELECT * FROM liked_recipe lr
            WHERE lr.recipe_id = :recipeId
                  AND lr.user_id = :userId
            """, nativeQuery = true)
    Optional<LikedRecipe> findLikedRecipeByUserIdAndRecipeId(
            @Param("userId") UUID userId,
            @Param("recipeId") UUID recipeId
    );

    @Query(value = """
            SELECT r.recipe_id,
                   r.creator_id,
                   c.image_id AS creator_image_id,
                   c.username AS creator_username,
                   r.recipe_name,
                   r.image_id AS recipe_image_id,
                   r.recipe_description,
                   r.ingredients,
                   r.steps,
                   r.likes_count,
                   r.views_count,
                   TRUE AS isLiked
            FROM recipe r
            INNER JOIN users c ON r.creator_id = c.user_id
            INNER JOIN liked_recipe lr
                ON r.recipe_id = lr.recipe_id
                 WHERE lr.user_id = :userId
            ORDER BY RANDOM()
            LIMIT :limit OFFSET :listOffset
            """, nativeQuery = true)
    List<Object[]> findLikedRecipesRandom(
            @Param("userId") UUID userId,
            @Param("limit") short limit,
            @Param("listOffset") int listOffset
    );

    @Query(value = """
            SELECT r.recipe_id,
                   r.creator_id,
                   c.image_id AS creator_image_id,
                   c.username AS creator_username,
                   r.recipe_name,
                   r.image_id AS recipe_image_id,
                   r.recipe_description,
                   r.ingredients,
                   r.steps,
                   r.likes_count,
                   r.views_count,
                   TRUE AS isLiked
            FROM recipe r
            INNER JOIN users c ON r.creator_id = c.user_id
            INNER JOIN liked_recipe lr ON r.recipe_id = lr.recipe_id
                WHERE LOWER(r.recipe_name) LIKE LOWER(CONCAT('%', :searchWord, '%'))
                AND (:ingredients IS NULL OR r.ingredients @> CAST(:ingredients AS text[]))
                AND lr.user_id = :userId
            LIMIT :limit OFFSET :listOffset
            """, nativeQuery = true)
    List<Object[]> findLikedRecipesWithSearchWordAndFilters(
            @Param("userId") UUID userId,
            @Param("searchWord") String searchWord,
            @Param("ingredients") List<String> ingredients,
            @Param("limit") short limit,
            @Param("listOffset") int listOffset
    );

    boolean existsByUser_UserIdAndRecipe_RecipeId(UUID userId, UUID recipeId);

    void deleteById(UUID likedRecipeId);
}
