package com.flavor.forge.Repo;

import com.flavor.forge.Model.FollowedCreator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowedCreatorRepo extends JpaRepository<FollowedCreator, UUID> {

    Optional<FollowedCreator> findByUser_UserIdAndCreator_UserId(UUID userId, UUID creatorId);

    List<FollowedCreator> findAllByUserId(UUID userId);

    List<FollowedCreator> findByUser_UserIdAndCreator_UsernameContainingIgnoreCase(UUID userId, String creatorName);

    boolean existsByUser_UserIdAndCreator_UserId(UUID userId, UUID creatorId);

    void deleteByFollowedId(UUID followedId);
    void deleteByUser_UserIdAndCreator_UserId(UUID userId, UUID creatorId);
}
