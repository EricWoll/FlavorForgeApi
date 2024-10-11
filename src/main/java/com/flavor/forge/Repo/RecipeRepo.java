package com.flavor.forge.Repo;

import com.flavor.forge.Model.Recipe;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepo extends MongoRepository<Recipe, ObjectId> {

    List<Recipe> findAllByUserId(ObjectId id);

    boolean existsByRecipeName(String name);
}
