package com.flavor.forge.Controller;

import com.flavor.forge.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("api/v2/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{user_id}")
    public ResponseEntity<User> findSingleUser(
            @PathVariable(value = "user_id") UUID userId
    ) {
        return new ResponseEntity<User>(
                userService.findSingleUser(userId),
                HttpStatus.OK
        );
    }

    @PutMapping("/{user_id}")
    public ResponseEntity<User> updateUser(
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "user") User user,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<User>(
                userService.updateUser(userId, user, accessToken),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<User> deleteUser(
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<User>(
                userService.deleteUser(userId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }

    @GetMapping("/{user_id}/followed")
    public ResponseEntity<List<User>> findFollowedCreators(
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "search_string", required = false) String searchString,
            @RequestParam(value = "access_token") String accessToken
    ) {
        boolean hasSearchString = searchString != null && !searchString.isEmpty();

        List<User> results;

        if (hasSearchString) {
            results = userService.findFollowedCreatorsWithSearch(userId, searchString, accessToken);
        } else {
            results = userService.findFollowedCreators(userId, accessToken);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/{user_id}/followed/{creator_id}")
    public ResponseEntity<User> addFollowedCreator(
            @PathVariable(value = "user_id") UUID userId,
            @PathVariable(value = "creator_id") UUID creatorId,
            @RequestParam(value = "is_followed") Boolean isFollowed,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<User>(
                userService.addFollowedCreator(userId, creatorId, isFollowed, accessToken),
                HttpStatus.NO_CONTENT
        );
    }

    @DeleteMapping("/{user_id}/followed/{creator_id}")
    public ResponseEntity<User> removeFollowedCreator(
            @PathVariable(value = "user_id") UUID userId,
            @PathVariable(value = "creator_id") UUID creatorId,
            @RequestParam(value = "access_token") String accessToken
    ) {
        return new ResponseEntity<User>(
                userService.removeFollowedCreator(userId, creatorId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }


}
