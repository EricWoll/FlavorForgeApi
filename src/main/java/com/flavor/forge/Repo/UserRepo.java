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

    Optional<User> findByUserId(String userId);

    Optional<User> findByUsername(String username);

    @Query(value = """
            SELECT u.user_id AS userId,
            u.username,
            u.image_id AS imageId,
            u.follower_count AS followerCount,
            u.about_text AS aboutText,
            CAST(
                CASE
                    WHEN fc.followed_user_id IS NOT NULL THEN true
                    ELSE false
                END AS BOOLEAN
            ) AS isFollowed
            FROM users u
            LEFT JOIN followed_creator fc
                ON fc.followed_creator_id = u.user_id
                AND fc.followed_user_id = :userId
            WHERE u.user_id = :creatorId
            """, nativeQuery = true)
    Optional<Object>findPublicUserByUserId(
            @Param("creatorId") String creatorId,
            @Param("userId") String userId
    );

    @Query(value = """
            SELECT u.user_id AS userId,
            u.username,
            u.email,
            u.image_id AS imageId,
            u.about_text AS aboutText
            FROM users u
                WHERE u.user_id = :userId
            """, nativeQuery = true)
    Optional<Object>findPrivateUserByUserId(
            @Param("userId") String userId
    );

    void deleteByUserId(String userId);
}
