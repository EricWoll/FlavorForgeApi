package com.flavor.forge.Controller;

import com.flavor.forge.Model.Comment;
import com.flavor.forge.Service.CommentService;
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

    @GetMapping("/recipes/{recipe_id}")
    public ResponseEntity<List<Comment>> findCommentsWithRecipe(
            @PathVariable(value = "recipe_id") UUID recipeId
    ) {
        return new ResponseEntity<List<Comment>>(
                commentService.findCommentsWithRecipe(recipeId),
                HttpStatus.OK
        );
    }


    @PostMapping("/")
    public ResponseEntity<Comment> createComment(
            @RequestParam(value = "comment") Comment comment,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<Comment>(
                commentService.createComment(comment, accessToken),
                HttpStatus.OK
        );
    }


    @PutMapping("/{comment_id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable(value = "comment_id") UUID commentId,
            @RequestParam(value = "comment") Comment commentBody,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<Comment>(
                commentService.updateComment(commentId, commentBody, accessToken),
                HttpStatus.OK
        );
    }


    @DeleteMapping("/{comment_id}")
    public ResponseEntity<Comment> deleteComment(
            @PathVariable(value = "comment_id") UUID commentId,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<Comment>(
                commentService.deleteComment(commentId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }
}
