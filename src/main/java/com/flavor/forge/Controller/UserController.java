package com.flavor.forge.Controller;

import com.flavor.forge.Model.User;
import com.flavor.forge.Model.Response.PublicUserResponse;
import com.flavor.forge.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser (@PathVariable String username, @RequestBody User payload) {
        return new ResponseEntity<User>(
                userService.updateUser(username, payload),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<User> deleteUser(@PathVariable String username, @RequestBody User payload) {
        return new ResponseEntity<User>(
                userService.deleteUser(payload, username),
                HttpStatus.NO_CONTENT
        );
    }
}
