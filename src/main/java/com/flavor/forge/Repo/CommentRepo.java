package com.flavor.forge.Repo;

import com.flavor.forge.Model.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends MongoRepository<Comment, ObjectId> {
    List<Comment> findAllByAttachedId(ObjectId id);
    List<Comment> findAllByUserId(ObjectId id);
}
