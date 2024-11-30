package com.flavor.forge.Controller;

import com.flavor.forge.Model.FollowedCreator;
import com.flavor.forge.Model.Response.FollowedCreatorResponse;
import com.flavor.forge.Model.Response.PublicCreatorResponse;
import com.flavor.forge.Model.User;
import com.flavor.forge.Model.Response.PublicUserResponse;
import com.flavor.forge.Service.UserService;
import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<PublicUserResponse> findSingleUser(@PathVariable String username) {
        return new ResponseEntity<PublicUserResponse>(
                userService.findOneByUsername(username),
                HttpStatus.OK
        );
    }

    @GetMapping("/id/{user_id}")
    public ResponseEntity<PublicUserResponse> findSingleUserById(@PathVariable String user_id) {
        return new ResponseEntity<PublicUserResponse>(
                userService.findOneById(user_id),
                HttpStatus.OK
        );
    }

    @GetMapping("/edit/{username}")
    public ResponseEntity<User> findSingleUserToEdit(@PathVariable String username) {
        return new ResponseEntity<User>(
                userService.findOneByUsernameToEdit(username),
                HttpStatus.OK
        );
    }

    @GetMapping("/creator/{creator_id}")
    public ResponseEntity<PublicCreatorResponse> findCreatorAndIsFollowed(
            @PathVariable String creator_id,
            @RequestParam("user_id") String user_id
    ) {
        return new ResponseEntity<PublicCreatorResponse>(
                userService.findCreatorAndIsFollowed(user_id, creator_id),
                HttpStatus.OK
        );
    }

    @GetMapping("/followed/{user_id}")
    public ResponseEntity<List<FollowedCreatorResponse>> findFollowedCreatorsByUserId(@PathVariable String user_id) {
        return new ResponseEntity<List<FollowedCreatorResponse>>(
                userService.findFollowedCreatorsByUserId(user_id),
                HttpStatus.OK
        );
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser (@PathVariable String username, @RequestBody User payload) {
        return new ResponseEntity<User>(
                userService.updateUser(username, payload),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/followed")
    public ResponseEntity<FollowedCreator> createFollowedCreator (
            @RequestParam("user_id") String user_id,
            @RequestParam("creator_id") String creator_id
        ) {
        return new ResponseEntity<FollowedCreator>(
                userService.createFollowedCreator(user_id, creator_id),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<User> deleteUser(@PathVariable String username, @RequestBody User payload) {
        return new ResponseEntity<User>(
                userService.deleteUser(payload, username),
                HttpStatus.NO_CONTENT
        );
    }

    @DeleteMapping("/followed")
    public ResponseEntity<DeleteResult> deleteFollow(
            @RequestParam("user_id") String user_id,
            @RequestParam("creator_id") String creator_id
            ) {
        return new ResponseEntity<DeleteResult>(
                userService.deleteFollowedCreator(user_id, creator_id),
                HttpStatus.NO_CONTENT
        );
    }

}
