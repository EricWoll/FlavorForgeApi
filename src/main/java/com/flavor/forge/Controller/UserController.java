package com.flavor.forge.Controller;

import com.flavor.forge.Model.DTO.FollowedCreatorDTO;
import com.flavor.forge.Model.DTO.PrivateUserDTO;
import com.flavor.forge.Model.DTO.PublicUserDTO;
import com.flavor.forge.Model.FollowedCreator;
import com.flavor.forge.Model.User;
import com.flavor.forge.Service.UserService;
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
@RequestMapping("api/v2/users")
public class UserController {

    @Autowired
    private UserService userService;

    private Utils utils;

    @GetMapping("/search/{creator_id}")
    public ResponseEntity<PublicUserDTO> findSingleUser(
            @PathVariable(value = "creator_id") UUID creatorId,
            @RequestParam(value = "user_id", required = false) UUID userId
    ) {
        if (!Utils.validateUUIDs(userId, creatorId)) {
            return ResponseEntity.badRequest().build();
        }

        return new ResponseEntity<PublicUserDTO>(
                userService.findSinglePublicUser(creatorId, userId),
                HttpStatus.OK
        );
    }

    @GetMapping("/profile/{user_id}")
    public ResponseEntity<PrivateUserDTO> findSinglePrivateUser(
            @PathVariable(value = "user_id") UUID userId,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(userId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<PrivateUserDTO>(
                userService.findSinglPrivateUser(userId, accessToken),
                HttpStatus.OK
        );
    }


    @GetMapping("/followed/search/{user_id}")
    public ResponseEntity<List<FollowedCreatorDTO>> findFollowedCreators(
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "search_string", required = false) String searchString,
            @RequestParam(value = "listOffset", defaultValue = "0") int listOffset,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(userId)) {
            return ResponseEntity.badRequest().build();
        }
        boolean hasSearchString = searchString != null && !searchString.isEmpty();

        List<FollowedCreatorDTO> results;

        if (hasSearchString) {
            results = userService.findFollowedCreatorsWithSearch(userId, searchString, accessToken, listOffset);
        } else {
            results = userService.findFollowedCreators(userId, accessToken, listOffset);
        }

        return ResponseEntity.ok(results);
    }


    @PostMapping("/followed/add/{user_id}")
    public ResponseEntity<FollowedCreator> addFollowedCreator(
            @Valid
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "creator_id") UUID creatorId,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(userId, creatorId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<FollowedCreator>(
                userService.addFollowedCreator(userId, creatorId, accessToken),
                HttpStatus.CREATED
        );
    }


    @PutMapping("/update/{user_id}")
    public ResponseEntity<User> updateUser(
            @Valid
            @PathVariable(value = "user_id") UUID userId,
            @RequestBody User user,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(userId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<User>(
                userService.updateUser(userId, user, accessToken),
                HttpStatus.OK
        );
    }


    @DeleteMapping("/delete/{user_id}")
    public ResponseEntity<User> deleteUser(
            @PathVariable(value = "user_id") UUID userId,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(userId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<User>(
                userService.deleteUser(userId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }

    @DeleteMapping("/followed/delete/{user_id}")
    public ResponseEntity<FollowedCreator> removeFollowedCreator(
            @PathVariable(value = "user_id") UUID userId,
            @RequestParam(value = "creator_id") UUID creatorId,
            @RequestHeader (name="Authorization") String accessToken
    ) {
        if (!Utils.validateUUIDs(userId, creatorId)) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<FollowedCreator>(
                userService.removeFollowedCreator(userId, creatorId, accessToken),
                HttpStatus.NO_CONTENT
        );
    }


}
