package com.flavor.forge.Repo;

import com.flavor.forge.Model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User, ObjectId> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // List Search Users (Regex)

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    void deleteByUsername(String username);
}
