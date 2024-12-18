package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.CommentEmptyException;
import com.flavor.forge.Exception.CustomExceptions.CommentNotFoundException;
import com.flavor.forge.Model.Comment;
import com.flavor.forge.Repo.CommentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentRepo commentRepo;

    public Comment findOneById(String id) {
        return commentRepo.findByCommentId(id)
                .orElseThrow(() -> {
                    logger.error("No comment found with id of: {}", id);
                    return new CommentNotFoundException("Comment Not Found for Id: " + id);
                });
    }

    public List<Comment> findAllByAttachedId(String id) {
        return commentRepo.findAllByAttachedId(id);
    }

    public List<Comment> findAllByUserId(String id) {
        return commentRepo.findAllByUserId(id);
    }

    public Comment createComment(Comment comment) {
        if (
                comment.getCommentText() == null || comment.getCommentText().isEmpty()
                        || comment.getUserId() == null || comment.getAttachedId() == null
        ){
            logger.error("Comment is missing some content and cannot be created!");
            throw new CommentEmptyException("Comment Is Missing Some Content!");
        }

        return commentRepo.insert(
                new Comment(
                        comment.getUserId(),
                        comment.getAttachedId(),
                        comment.getCommentText()
                )
        );
    }

    public Comment updateComment(String id, Comment comment) {
        if (
                comment.getCommentText() == null || comment.getCommentText().isEmpty()
                        || comment.getUserId() == null || comment.getAttachedId() == null
        ){
            logger.error("Comment with Id of \"{}\" is missing some content and cannot be updated!", id);
            throw new CommentEmptyException("Comment Is Missing Some Content!!");
        }

        Comment foundComment = commentRepo.findByCommentId(id)
                .orElseThrow(() -> {
                    logger.error("Comment does not exists with Id of \"{}\" and cannot be updated!", id);
                    return new CommentNotFoundException("Comment Not Found With Id Of: " + id);
                });

        foundComment.setCommentText(comment.getCommentText());

        commentRepo.save(foundComment);
        return foundComment;
    }

    public Comment deleteCommentById(String id) {
        Comment comment = commentRepo.findByCommentId(id)
                .orElseThrow(() -> {
                    logger.error("Comment does not exists with Id of \"{}\" and cannot be deleted!", id);
                    return new CommentNotFoundException("Comment Not Found for Id: " + id);
                });

        commentRepo.deleteByCommentId(id);
        return comment;
    }
}
