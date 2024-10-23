package com.flavor.forge.Repo;

import com.flavor.forge.Model.LikedRecipe;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikedRecipeRepo extends MongoRepository<LikedRecipe, ObjectId> {

    Optional<LikedRecipe> findByRecipeId(ObjectId id);

    List<LikedRecipe> findAllByUserId(ObjectId userId);
}
