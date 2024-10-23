package com.flavor.forge.Repo;

import com.flavor.forge.Model.Recipe;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepo extends MongoRepository<Recipe, ObjectId> {

    // List Search Recipes (Regex)

    List<Recipe> findAllByUserId(ObjectId userId);

    boolean existsByRecipeName(String name);
}
