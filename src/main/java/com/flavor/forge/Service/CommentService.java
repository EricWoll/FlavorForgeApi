package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.CommentEmptyException;
import com.flavor.forge.Exception.CustomExceptions.CommentExistsException;
import com.flavor.forge.Exception.CustomExceptions.CommentNotFoundException;
import com.flavor.forge.Model.Comment;
import com.flavor.forge.Repo.CommentRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepo commentRepo;

    public Comment findOneById(ObjectId id) {
        return commentRepo.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment Not Found for Id: " + id));
    }

    public List<Comment> findAllByAttachedId(ObjectId id) {
        return commentRepo.findAllByAttachedId(id);
    }

    public Comment createComment(Comment comment) {
        if (
                comment.getCommentText() == null || comment.getCommentText().isEmpty()
                        || comment.getUserId() == null || comment.getAttachedId() == null
        ){
            throw new CommentEmptyException("Comment Is Missing Some Content!");
        }

        if (commentRepo.existsById(comment.getId())) {
            throw new CommentExistsException("Comment Already Exists With Id Of: " + comment.getId());
        }

        return commentRepo.insert(
                new Comment(
                        comment.getUserId(),
                        comment.getAttachedId(),
                        comment.getCommentText()
                )
        );
    }

    public Comment updateComment(ObjectId id, Comment comment) {
        if (
                comment.getCommentText() == null || comment.getCommentText().isEmpty()
                        || comment.getUserId() == null || comment.getAttachedId() == null
        ){
            throw new CommentEmptyException("Comment Is Missing Some Content!!");
        }

        Comment foundComment = commentRepo.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment Not Found With Id Of: " + id));

        foundComment.setCommentText(comment.getCommentText());

        commentRepo.save(foundComment);
        return foundComment;
    }

    public Comment deleteCommentById(ObjectId id) {
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment Not Found for Id: " + id));

        commentRepo.deleteById(id);
        return comment;
    }
}
