package com.flavor.forge.Repo;

import com.flavor.forge.Model.FollowedCreator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowedCreatorRepo extends JpaRepository<FollowedCreator, UUID> {

    List<FollowedCreator> findAllByUserId(UUID userId);

    void deleteByFollowedId(UUID followedId);
}
