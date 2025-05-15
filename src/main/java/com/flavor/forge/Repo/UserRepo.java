package com.flavor.forge.Repo;

import com.flavor.forge.Model.DTO.PublicUserDTO;
import com.flavor.forge.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    Optional<User> findByUserId(UUID userId);

    @Query(value = """
            SELECT u.user_id AS userId,
            u.username,
            u.image_id AS imageId,
            u.follower_count AS followerCount,
            u.about_text AS aboutText,
            CASE
                WHEN :userId IS NOT NULL AND EXISTS (
                    SELECT 1 FROM followed_creator fc
                    WHERE fc.user_id = :userId AND fc.creator_id = :creatorId
                ) THEN true
                ELSE false
            END AS isFollowed
            FROM users u
                WHERE u.user_id = :creatorId
            """, nativeQuery = true)
    Optional<Object>findPublicUserByUserId(
            @Param("creatorId") UUID creatorId,
            @Param("userId") UUID userId
    );

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUserId(UUID userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    void deleteByUserId(UUID userId);
}
