package com.flavor.forge.Controller;

import com.flavor.forge.Model.Comment;
import com.flavor.forge.Service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{comment_id}")
    public ResponseEntity<Comment> findSingleComment(@PathVariable String comment_id) {
        return new ResponseEntity<Comment>(
                commentService.findOneById(comment_id),
                HttpStatus.OK
        );
    }

    @GetMapping("/attached/{attached_id}")
    public ResponseEntity<List<Comment>> findAllCommentsByAttached(@PathVariable String attached_id) {
        return new ResponseEntity<List<Comment>>(
                commentService.findAllByAttachedId(attached_id),
                HttpStatus.OK
        );
    }

    @GetMapping("/users/{user_id}")
    public ResponseEntity<List<Comment>> findAllCommentsByUser(@PathVariable String user_id) {
        return new ResponseEntity<List<Comment>>(
                commentService.findAllByUserId(user_id),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment payload) {
        return new ResponseEntity<Comment>(
                commentService.createComment(payload),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{comment_id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable String comment_id,
            @RequestBody Comment payload
    ) {
        return new ResponseEntity<Comment>(
                commentService.updateComment(
                        comment_id,
                        payload
                ),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{comment_id}")
    public ResponseEntity<Comment> deleteComment(@PathVariable String comment_id) {
        return new ResponseEntity<Comment>(
                commentService.deleteCommentById(comment_id),
                HttpStatus.NO_CONTENT
        );
    }
}
