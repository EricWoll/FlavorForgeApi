package com.flavor.forge.Repo;

import com.flavor.forge.Model.FollowedCreator;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowedCreatorRepo extends MongoRepository<FollowedCreator, ObjectId> {
    List<FollowedCreator> findAllByUserId(ObjectId id);
}
