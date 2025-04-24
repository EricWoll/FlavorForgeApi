package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.CommentEmptyException;
import com.flavor.forge.Exception.CustomExceptions.CommentNotFoundException;
import com.flavor.forge.Exception.CustomExceptions.DatabaseCRUDException;
import com.flavor.forge.Model.Comment;
import com.flavor.forge.Repo.CommentRepo;
import com.flavor.forge.Security.Jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private JwtService jwtService;

    public List<Comment> findCommentsWithRecipe(UUID recipeId) {
        return commentRepo.findAllByAttachedId(recipeId);
    }

    public Comment createComment(Comment comment, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, comment.getUserId());

        if (
                comment.getCommentText() == null || comment.getCommentText().isEmpty()
                        || comment.getUserId() == null || comment.getAttachedId() == null
        ){
            logger.error("Comment is missing some content and cannot be created!");
            throw new CommentEmptyException("Comment Is Missing Some Content!");
        }
        try {
            return commentRepo.save(
                    new Comment(
                            comment.getUserId(),
                            comment.getAttachedId(),
                            comment.getCommentText()
                    )
            );
        }  catch (DataAccessException e) {
            logger.error("Database error during data creation: \"{}\"", e.getMessage());
            throw new DatabaseCRUDException("Database error during data creation: " + e.getMessage());
        }
    }

    public Comment updateComment(UUID commentId, Comment commentBody, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        if (
                commentBody.getCommentText() == null || commentBody.getCommentText().isEmpty()
                        || commentBody.getUserId() == null || commentBody.getAttachedId() == null
        ){
            logger.error("Comment with Id of \"{}\" is missing some content and cannot be updated!", commentId);
            throw new CommentEmptyException("Comment Is Missing Some Content!!");
        }

        Comment foundComment = commentRepo.findByCommentId(commentId)
                .orElseThrow(() -> {
                    logger.error("Comment does not exists with Id of \"{}\" and cannot be updated!", commentId);
                    return new CommentNotFoundException("Comment Not Found With Id Of: " + commentId);
                });

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, foundComment.getUserId());

        foundComment.setCommentText(commentBody.getCommentText());

        try {
            commentRepo.save(foundComment);
        } catch (DataAccessException e) {
            logger.error("Database error during data update: \"{}\"", e.getMessage());
            throw new DatabaseCRUDException("Database error during data update: " + e.getMessage());
        }

        return foundComment;
    }

    public Comment deleteComment(UUID commentId, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        Comment foundComment = commentRepo.findByCommentId(commentId).orElseThrow(()-> {
            logger.error("Comment does not exists with Id of \"{}\" and cannot be deleted!", commentId);
            return new CommentNotFoundException("Comment Not Found for Id: " + commentId);
        });

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, foundComment.getUserId());

        try {
            commentRepo.deleteByCommentId(foundComment.getCommentId());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid comment ID: \"{}\"", e.getMessage());
            throw new IllegalArgumentException("Invalid comment ID: " + e.getMessage());

        } catch (DataAccessException e) {
            logger.error("Database error during data deletion: \"{}\"", e.getMessage());
            throw new DatabaseCRUDException("Database error during data deletion: " + e.getMessage());
        }

        return foundComment;
    }
}
