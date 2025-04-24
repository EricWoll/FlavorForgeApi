package com.flavor.forge.Repo;

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

    Optional<FollowedCreator> findByUser_UserIdAndCreator_UserId(UUID userId, UUID creatorId);

    @Query(value = """
            SELECT * FROM followed_creator fc
                INNER JOIN user u ON fc.user_id = u.user_id
                WHERE fc.user_id = :userId
            LIMIT :limit OFFSET :listOffset
            """, nativeQuery = true)
    List<FollowedCreator> findAllByUserId(
            @Param("userId") UUID userId,
            @Param("limit") short limit,
            @Param("listOffset") int listOffset
    );

    @Query(value = """
            SELECT * FROM followed_creator fc
                INNER JOIN user u ON fc.user_id = u.user_id
                WHERE fc.user_id = :userId
                AND LOWER(u.username) LIKE LOWER(CONCAT('%', :creatorName, '%'))
                LIMIT :limit OFFSET :listOffset
            """, nativeQuery = true)
    List<FollowedCreator> searchWithString(
            @Param("userId") UUID userId,
            @Param("creatorName") String creatorName,
            @Param("limit") short limit,
            @Param("listOffset") int listOffset
    );

    boolean existsByUser_UserIdAndCreator_UserId(UUID userId, UUID creatorId);

    void deleteByFollowedId(UUID followedId);
    void deleteByUser_UserIdAndCreator_UserId(UUID userId, UUID creatorId);
}
