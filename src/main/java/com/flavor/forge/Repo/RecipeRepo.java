package com.flavor.forge.Repo;

import com.flavor.forge.Model.Recipe;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepo extends MongoRepository<Recipe, String> {

    Optional<Recipe> findByRecipeId(String recipeId);

    @Aggregation( pipeline = { "{ $sample: { size: ?0 } }" })
    List<Recipe> defaultSearchInRecipes(int pageAmount);

    @Query("{" +
                "'recipeName': { $regex: /?0.*/, $options: 'i'}," +
                "'ingredients.name': { $in: ?1 }" +
            "}")
    List<Recipe> findAllByRecipeNameAndIngredients(String searchString, List<String> ingredients);

    @Query("{ 'userId': ?0, 'recipeName': { $regex: /?1.*/, $options: 'i'} }")
    List<Recipe> findAllByUserIdAndSearchString(String userId, String searchString);

    @Query("{ 'recipeName': { $regex: /?0.*/, $options: 'i'} }")
    List<Recipe> findAllByRecipeName(String searchString);

    @Query("{ 'userId': ?0 }")
    List<Recipe> findAllByUserId(String userId);

    boolean existsByRecipeName(String name);

    void deleteByRecipeId(String recipeId);
}

