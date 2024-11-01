package com.flavor.forge.Repo;

import com.flavor.forge.Model.Recipe;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepo extends MongoRepository<Recipe, ObjectId> {

    @Query("{RecipeName: { $regex: /?0.*/, $options: 'i'}, Ingredients: { $all: ?1 , $options: 'i' } }")
    List<Recipe> findAllByRecipeName(String searchString, List<String> ingredients);

    List<Recipe> findAllByUserId(ObjectId userId);

    boolean existsByRecipeName(String name);
}
