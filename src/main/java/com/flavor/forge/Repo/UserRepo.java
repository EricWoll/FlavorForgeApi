package com.flavor.forge.Repo;

import com.flavor.forge.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    Optional<User> findByUserId(UUID userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUserId(UUID userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    void deleteByUserId(UUID userId);
}
