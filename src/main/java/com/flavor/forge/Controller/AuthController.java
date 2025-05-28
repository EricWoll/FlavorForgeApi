package com.flavor.forge.Controller;

import com.flavor.forge.Model.DTO.AuthDTO;
import com.flavor.forge.Model.User;
import com.flavor.forge.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/v2/auth")
public class AuthController {

    @Autowired
   private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTO> loginUser(@RequestBody User user) {
        return new ResponseEntity<AuthDTO>(
                userService.login(user),
                HttpStatus.OK
        );
    }


    @PostMapping("/signup")
    public ResponseEntity<AuthDTO> registerUser(@RequestBody User user) {
        return new ResponseEntity<AuthDTO>(
                userService.createUser(user),
                HttpStatus.OK
        );
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthDTO> refreshUser(
            HttpServletRequest request
    ) {
        return new ResponseEntity<AuthDTO>(
                userService.refreshJwToken(request),
                HttpStatus.OK
        );
    }


//    @PostMapping("/forgot_password")
//    public ResponseEntity<Boolean> sendEmailForgottenPassword(
//            @RequestParam("user_email") String userEmail
//            ) {
//        return new ResponseEntity<Boolean>(
//                userService.generatePasswordResetIdWithEmail(userEmail),
//                HttpStatus.NO_CONTENT
//        );
//    }


//    @PostMapping("/generate_unique_uuid/{user_id}")
//    public ResponseEntity<UUID> generateUniqueUUIDForPassword(
//        @RequestParam("user_id") UUID userId,
//        @RequestParam("user_password") String userPassword,
//        @RequestParam(value = "access_token") String accessToken
//    ) {
//        return new ResponseEntity<UUID>(
//                userService.generateUniqueId(userId, userPassword, accessToken),
//                HttpStatus.OK
//        );
//    }


//    @PostMapping("/update_password")
//    public  ResponseEntity<Boolean> updateUserPassword(
//            @RequestParam("unique_uuid") UUID uniqueUUID,
//            @RequestParam("new_user_password") String newUserPassword
//    ) {
//        return new ResponseEntity<Boolean>(
//                userService.updatePassword(uniqueUUID, newUserPassword),
//                HttpStatus.NO_CONTENT
//        );
//    }
}
