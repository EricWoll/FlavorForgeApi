package com.flavor.forge.Controller;

import com.flavor.forge.Model.AuthResponse;
import com.flavor.forge.Model.User;
import com.flavor.forge.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody User user) {
        return new ResponseEntity<AuthResponse>(
                userService.createUser(user),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody User user) {
        return new ResponseEntity<AuthResponse> (
                userService.login(user),
                HttpStatus.OK
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshUser(
            HttpServletRequest request
    ) {
        return new ResponseEntity<AuthResponse>(
                userService.refreshJwtToken(request),
                HttpStatus.OK
        );
    }
}
