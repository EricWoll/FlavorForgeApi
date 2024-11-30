package com.flavor.forge.Repo;

import com.flavor.forge.Model.FollowedCreator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowedCreatorRepo extends MongoRepository<FollowedCreator, String> {
    List<FollowedCreator> findAllByUserId(String userId);

    @Query("{'userId': ?0, 'creatorId': ?1}")
    Optional<FollowedCreator> findByUserIdAndCreatorId(String userId, String creatorId);


    void deleteByFollowedId(String followedId);
}
