package com.flavor.forge.Repo;

import com.flavor.forge.Model.DTO.FollowedCreatorDTO;
import com.flavor.forge.Model.FollowedCreator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowedCreatorRepo extends JpaRepository<FollowedCreator, UUID> {

    Optional<FollowedCreator> findByUser_UserIdAndCreator_UserId(String userId, String creatorId);

    @Query(value = """
            SELECT
                u.user_id,
                u.username,
                u.image_id,
                u.follower_count,
                u.about_text
            FROM followed_creator fc
                INNER JOIN users u ON fc.followed_creator_id = u.user_id
                WHERE fc.followed_user_id = :userId
            LIMIT :limit OFFSET :listOffset
            """, nativeQuery = true)
    List<Object[]> findAllByUserId(
            @Param("userId") String userId,
            @Param("limit") short limit,
            @Param("listOffset") int listOffset
    );

    @Query(value = """
            SELECT
                u.user_id,
                u.username,
                u.image_id,
                u.follower_count,
                u.about_text
            FROM followed_creator fc
                INNER JOIN users u ON fc.followed_creator_id = u.user_id
                WHERE fc.followed_user_id = :userId
                AND LOWER(u.username) LIKE LOWER(CONCAT('%', :creatorName, '%'))
                LIMIT :limit OFFSET :listOffset
            """, nativeQuery = true)
    List<Object[]> searchWithString(
            @Param("userId") String userId,
            @Param("creatorName") String creatorName,
            @Param("limit") short limit,
            @Param("listOffset") int listOffset
    );

    boolean existsByUser_UserIdAndCreator_UserId(String userId, String creatorId);

    void deleteByUser_UserIdAndCreator_UserId(String userId, String creatorId);
}
