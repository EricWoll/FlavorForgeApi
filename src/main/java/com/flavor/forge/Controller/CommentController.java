package com.flavor.forge.Controller;

import com.flavor.forge.Model.Comment;
import com.flavor.forge.Service.CommentService;
import com.flavor.forge.Utils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("api/v2/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    private Utils utils;

    @GetMapping("/search/{recipe_id}")
    public ResponseEntity<List<Comment>> findCommentsWithRecipe(
            @PathVariable(value = "recipe_id") UUID recipeId
    ) {
        if (!Utils.validateUUIDs(recipeId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<List<Comment>>(
                commentService.findCommentsWithRecipe(recipeId),
                HttpStatus.OK
        );
    }


    @PostMapping("/create")
    public ResponseEntity<Comment> createComment(
            @Valid
            @RequestParam(value = "comment") Comment comment,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        return new ResponseEntity<Comment>(
                commentService.createComment(comment, accessToken),
                HttpStatus.OK
        );
    }


    @PutMapping("/update/{comment_id}")
    public ResponseEntity<Comment> updateComment(
            @Valid
            @PathVariable(value = "comment_id") UUID commentId,
            @RequestParam(value = "comment") Comment commentBody,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(commentId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<Comment>(
                commentService.updateComment(commentId, commentBody, accessToken),
                HttpStatus.OK
        );
    }


    @DeleteMapping("/delete/{comment_id}")
    public ResponseEntity<Comment> deleteComment(
            @PathVariable(value = "comment_id") UUID commentId,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(commentId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<Comment>(
                commentService.deleteComment(commentId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }
}
