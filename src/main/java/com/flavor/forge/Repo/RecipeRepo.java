package com.flavor.forge.Repo;

import com.flavor.forge.Model.Recipe;
import jakarta.persistence.SqlResultSetMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe, UUID> {

    @Query(value = """
                SELECT
                r.recipe_id AS recipeId,
               r.creator_id AS creatorId,
               c.image_id AS creatorImageId,
               c.username AS creatorUsername,
               r.recipe_name AS recipeName,
               r.image_id AS recipeImageId,
               r.recipe_description AS recipeDescription,
               r.ingredients AS ingredients,
               r.steps AS steps,
               r.likes_count AS likesCount,
               r.views_count AS viewsCount
            FROM recipe r
            INNER JOIN users c ON r.creator_id = c.user_id
                WHERE r.recipe_id = :recipeId
            """, nativeQuery = true)
    Optional<Object> findByRecipeIdWithCreator(@Param("recipeId") UUID recipeId);

    @Query(value = """
    SELECT
        r.recipe_id,
        r.creator_id,
        r.recipe_name,
        r.image_id,
        r.recipe_description,
        r.ingredients,
        r.steps,
        r.likes_count,
        r.views_count,
        r.is_private
    FROM recipe r
    WHERE r.recipe_id = :recipeId
    """, nativeQuery = true)
    Optional<Recipe> findByRecipeId(@Param("recipeId") UUID recipeId);

    @Query(value = """
        SELECT r.recipe_id AS recipeId,
               r.creator_id AS creatorId,
               c.image_id AS creatorImageId,
               c.username AS creatorUsername,
               r.recipe_name AS recipeName,
               r.image_id AS recipeImageId,
               r.recipe_description AS recipeDescription,
               r.ingredients AS ingredients,
               r.steps AS steps,
               r.likes_count AS likesCount,
               r.views_count AS viewsCount
        FROM recipe r
        INNER JOIN users c ON r.creator_id = c.user_id
        WHERE (:creatorId IS NULL OR r.creator_id = :creatorId)
        ORDER BY RANDOM()
        LIMIT :limit OFFSET :listOffset
        """, nativeQuery = true)
    List<Object[]> findRandomRecipes(
            @Param("limit") short limit,
            @Param("listOffset") int listOffset,
            @Param("creatorId") UUID creatorId
    );

    @Query(value = """
            SELECT r.recipe_id AS recipeId,
               r.creator_id AS creatorId,
               c.image_id AS creatorImageId,
               c.username AS creatorUsername,
               r.recipe_name AS recipeName,
               r.image_id AS recipeImageId,
               r.recipe_description AS recipeDescription,
               r.ingredients AS ingredients,
               r.steps AS steps,
               r.likes_count AS likesCount,
               r.views_count AS viewsCount
            FROM recipe r
            INNER JOIN users c ON r.creator_id = c.user_id
                WHERE (:searchWord IS NULL OR LOWER(r.recipe_name) LIKE LOWER(CONCAT('%', :searchWord, '%')))
                AND (:ingredientsJson IS NULL OR r.ingredients @> CAST(:ingredientsJson AS jsonb))
                AND (:creatorId IS NULL OR r.creator_id = :creatorId)
            ORDER BY RANDOM()
            LIMIT :limit OFFSET :listOffset
            """, nativeQuery = true)
    List<Object[]> findRecipesWithSearchWordAndIngredients(
            @Param("searchWord") String searchWord,
            @Param("ingredientsJson") String ingredientsJson,
            @Param("creatorId") UUID creatorId,
            @Param("limit") short limit,
            @Param("listOffset") int listOffset
    );

    boolean existsByRecipeName(String name);
    boolean existsByRecipeId(UUID recipeId);

    void deleteByRecipeId(UUID recipeId);
}
