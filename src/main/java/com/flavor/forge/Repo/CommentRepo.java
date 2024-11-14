package com.flavor.forge.Repo;

import com.flavor.forge.Model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepo extends MongoRepository<Comment, String> {
    Optional<Comment> findByCommentId(String commentId);
    List<Comment> findAllByAttachedId(String id);
    List<Comment> findAllByUserId(String id);

    void deleteByCommentId(String commentId);
}
