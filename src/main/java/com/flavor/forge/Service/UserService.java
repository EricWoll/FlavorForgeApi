package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.UserExistsException;
import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.AuthResponse;
import com.flavor.forge.Model.ERole;
import com.flavor.forge.Model.Response.PublicUserResponse;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.UserRepo;
import com.flavor.forge.Security.Jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;

    @Value("${forge.app.noImage}")
    private String noImageId;

    @Autowired
    private AuthenticationManager authManager;

    private BCryptPasswordEncoder BcpEncoder =  new BCryptPasswordEncoder(10);

    public PublicUserResponse findOneByUsername(String username) {
        User foundUser = userRepo.findByUsername(username)
                .orElseThrow(()-> {
                    logger.error("User not found with username of: {}.", username);
                    return new UserNotFoundException("User Does Not Exist!");
                });

        return PublicUserResponse.builder()
                .userId(foundUser.getUserId())
                .username(foundUser.getUsername())
                .imageId(foundUser.getImageId())
                .followerCount(foundUser.getFollowerCount())
                .aboutText(foundUser.getAboutText())
                .role(foundUser.getRole())
                .build();
    }
    public PublicUserResponse findOneById(String userId) {
        User foundUser = userRepo.findByUserId(userId)
                .orElseThrow(()-> {
                    logger.error("User not found with id of: {}.", userId);
                    return new UserNotFoundException("User Does Not Exist!");
                });

        return PublicUserResponse.builder()
                .username(foundUser.getUsername())
                .imageId(foundUser.getImageId())
                .followerCount(foundUser.getFollowerCount())
                .aboutText(foundUser.getAboutText())
                .role(foundUser.getRole())
                .build();
    }

    public AuthResponse createUser(User user) {

        if (userRepo.existsByUsername(user.getUsername())) {
            logger.error("User already exists with username of: {}.", user.getUsername());
            throw new UserExistsException("Username Already Exists!");
        }
        if (userRepo.existsByEmail(user.getEmail())) {
            logger.error("USer already exists with email of: {}.", user.getEmail());
            throw new UserExistsException("Email Already Exists!");
        }

        if (user.getImageId() == null) {
            logger.info("No imageId for created User, setting to empty String.");
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
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        User foundUser = userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User with username of \"{}\" is missing some content and cannot be updated!", username);
                    return new UserNotFoundException("User Does Not Exist!");
                });

        foundUser.setUsername(user.getUsername());
        foundUser.setEmail(user.getEmail());
        foundUser.setPassword(BcpEncoder.encode(user.getPassword()));
        foundUser.setImageId(user.getImageId());
        foundUser.setAboutText(user.getAboutText());

        userRepo.save(foundUser);
        return foundUser;
    }

    public User deleteUser(User user, String username) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        user.getPassword()
                )
        );

        User foundUser = userRepo.findByUsername(username)
                .orElseThrow(()-> {
                    logger.error("User does not exists with username of \"{}\" and cannot be updated!", username);
                    return new UserNotFoundException("User Does Not Exist!");
                });

        userRepo.deleteByUsername(foundUser.getUsername());
        return foundUser;
    }

    public AuthResponse login(User user) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        User foundUser = userRepo.findByUsername(user.getUsername())
                .orElseThrow(()-> {
                    logger.error("Username was not found in Database, cannot authenticate user!");
                    return new UserNotFoundException("User Was not Found!");
                });

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
                .userId(user.getUserId())
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
                        .orElseThrow(()-> {
                            logger.error("Username was not found in Database, cannot create Token!");
                            return new UsernameNotFoundException("Username Does Not Exist!");
                        });

                String accessToken = jwtService.generateJwtToken(user);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .userId(user.getUserId())
                        .role(user.getRole())
                        .build();
            }
        }
        return null;
    }
}
