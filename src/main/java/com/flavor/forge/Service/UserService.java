package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.UserExistsException;
import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.AuthResponse;
import com.flavor.forge.Model.ERole;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.FollowedCreatorRepo;
import com.flavor.forge.Repo.UserRepo;
import com.flavor.forge.Security.Jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowedCreatorRepo followedCreatorRepo;

    @Autowired
    private JwtService jwtService;

    @Value("adopt.app.noImage")
    private String noImageId;

    @Autowired
    private AuthenticationManager authManager;

    private BCryptPasswordEncoder BcpEncoder =  new BCryptPasswordEncoder(10);

    public User findOneByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(()-> new UserNotFoundException("User Does Not Exist!"));
    }
    public User findOneById(ObjectId userId) {
        return userRepo.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("User Does Not Exist!"));
    }

    public AuthResponse createUser(User user) {

        if (userRepo.existsByUsername(user.getUsername())) {
            throw new UserExistsException("Username Already Exists!");
        }
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new UserExistsException("Email Already Exists!");
        }

        if (user.getImageId() == null) {
            user.setImageId(noImageId);
        }

        User createdUser = userRepo.insert(
                new User(
                        user.getUsername(),
                        user.getEmail(),
                        BcpEncoder.encode(user.getPassword()),
                        user.getImageId(),
                        0,
                        "",
                        ERole.FREE
                )
        );

        return JwtToken(createdUser);
    }

    public User updateUser(String username, User user) {
        User foundUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User Does Not Exist!"));

        foundUser.setUsername(user.getUsername());
        foundUser.setEmail(user.getEmail());
        foundUser.setPassword(BcpEncoder.encode(user.getPassword()));
        foundUser.setImageId(user.getImageId());
        foundUser.setAboutText(user.getAboutText());

        userRepo.save(foundUser);
        return foundUser;
    }

    public User deleteUser(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(()-> new UserNotFoundException("User Does Not Exist!"));

        userRepo.deleteByUsername(username);
        return user;
    }

    public AuthResponse login(User user) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        User foundUser = userRepo.findByUsername(user.getUsername())
                .orElseThrow(()-> new UserNotFoundException("User Was not Found!"));

        return JwtToken(foundUser);

    }

    private AuthResponse JwtToken(User user) {
        String accessToken = jwtService.generateJwtToken(user);
        String refreshToken = jwtService.generateJwtRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .userId(user.getId())
                .role(user.getRole())
                .imageId(user.getImageId())
                .build();
    }

    public AuthResponse refreshJwtToken(
            HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");

        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")){
            String refreshToken = authHeader.substring(7);

            if (StringUtils.hasText(refreshToken) && jwtService.validateToken(refreshToken)) {
                String username = jwtService.getUsername(refreshToken);
                User user = userRepo.findByUsername(username)
                        .orElseThrow(()-> new UsernameNotFoundException("Username Does Not Exist!"));

                String accessToken = jwtService.generateJwtToken(user);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .userId(user.getId())
                        .role(user.getRole())
                        .build();
            }
        }
        return null;
    }
}
