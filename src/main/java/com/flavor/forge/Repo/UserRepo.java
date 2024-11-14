package com.flavor.forge.Repo;

import com.flavor.forge.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User, String> {

    Optional<User> findByUserId(String userId);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("{Username: { $regex: /?0.*/, $options: 'i' }")
    List<User> findAllByUsername(String searchString);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    void deleteByUsername(String username);
}
