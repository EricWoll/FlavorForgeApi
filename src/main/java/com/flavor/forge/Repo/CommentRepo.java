package com.flavor.forge.Repo;

import com.flavor.forge.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepo extends JpaRepository<Comment, UUID> {

    Optional<Comment> findByCommentId(UUID commentId);
    List<Comment> findAllByAttachedId(UUID attachedId);
    List<Comment> findAllByUserId(UUID userId);

    void deleteByCommentId(UUID commentId);
}
