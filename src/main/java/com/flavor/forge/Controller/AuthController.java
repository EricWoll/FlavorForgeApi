package com.flavor.forge.Controller;

import com.flavor.forge.Model.ProcessedWebHookEvent;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.ProcessedWebHookEventRepository;
import com.flavor.forge.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v2/auth")
public class AuthController {

    @Autowired
   private UserService userService;

    @Autowired
    private ProcessedWebHookEventRepository processedWebHookEventRepository;

    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(
            @RequestHeader("svix-id") String eventId,
            @RequestBody User user
    ) {
        Optional<ProcessedWebHookEvent> existingEvent = processedWebHookEventRepository.findByEventId(eventId);
        if (existingEvent.isPresent()) {
            // Event has already been processed; return a safe response.
            return new ResponseEntity<>(HttpStatus.OK);
        }

        User createdUser = userService.createUser(user, eventId);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}
