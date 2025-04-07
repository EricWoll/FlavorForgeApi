package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.DatabaseCRUDException;
import com.flavor.forge.Exception.CustomExceptions.UserExistsException;
import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.AuthResponse;
import com.flavor.forge.Model.ERole;
import com.flavor.forge.Model.FollowedCreator;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.FollowedCreatorRepo;
import com.flavor.forge.Repo.UserRepo;
import com.flavor.forge.Security.Jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowedCreatorRepo followedCreatorRepo;

    @Autowired
    private JwtService jwtService;

    @Value("${forge.app.noImage}")
    private String noImageId;

    @Autowired
    private AuthenticationManager authManager;

    private BCryptPasswordEncoder BcpEncoder = new BCryptPasswordEncoder(10);

    public User findSingleUser(UUID userId) {
        User foundUser = userRepo.findByUserId(userId).orElseThrow(() -> {
            logger.error("User not found with userId of: {}.\"", userId);
            return new UserNotFoundException("User not found with userId of: " + userId);
        });

        // Make this return a DTO
        return foundUser;
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
            user.setImageId(noImageId);
        }

        User createdUser = userRepo.save(
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
        return JwToken(createdUser);
    }

    public User updateUser(UUID userId, User user, String accessToken) {
        if (!jwtService.validateToken(accessToken)) {
            throw new BadCredentialsException("Bad Credentials");
        }

        String queuedUsername = jwtService.getUsername(accessToken);

        User foundUser = userRepo.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("User with userId of \"{}\" is missing some content and cannot be updated!", userId);
                    return new UserNotFoundException("Some User information is missing!");
                });

        if (!queuedUsername.equals(foundUser.getUsername())) {
            throw new BadCredentialsException("User queued for Deletion is not the same as the Active user!");
        }

        foundUser.setUsername(user.getUsername());
        foundUser.setEmail(user.getEmail());
        foundUser.setImageId(user.getImageId());
        foundUser.setAboutText(user.getAboutText());

        userRepo.save(foundUser);
        return foundUser;
    }

    public User deleteUser(UUID userId, String accessToken) {
        if (!jwtService.validateToken(accessToken)) {
            throw new BadCredentialsException("Bad Credentials");
        }

        String queuedUsername = jwtService.getUsername(accessToken);

        User foundUser = userRepo.findByUserId(userId)
                .orElseThrow(()-> {
                    logger.error("User does not exists with userId of \"{}\" and cannot be updated!", userId);
                    return new UserNotFoundException("User Does Not Exist!");
                });

        if (!queuedUsername.equals(foundUser.getUsername())) {
            throw new BadCredentialsException("User queued for Deletion is not the same as the Active user!");
        }

        userRepo.deleteByUserId(userId);
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

        return JwToken(foundUser);
    }

    public List<FollowedCreator> findFollowedCreators(UUID userId, String accessToken) {
        if (!jwtService.validateToken(accessToken)) {
            throw new BadCredentialsException("Bad Credentials");
        };

        return followedCreatorRepo.findAllByUserId(userId);
    }

    public List<FollowedCreator> findFollowedCreatorsWithSearch(UUID userId, String searchString, String accessToken) {
        if (!jwtService.validateToken(accessToken)) {
            throw new BadCredentialsException("Bad Credentials");
        };

        return followedCreatorRepo.findByUser_UserIdAndCreator_UsernameContainingIgnoreCase(userId, searchString);
    }

    public FollowedCreator addFollowedCreator(UUID userId, UUID creatorId, String accessToken) {
        if (!jwtService.validateToken(accessToken)) {
            throw new BadCredentialsException("Bad Credentials");
        };

        String queuedUsername = jwtService.getUsername(accessToken);

        if (followedCreatorRepo.existsByUser_UserIdAndCreator_UserId(userId, creatorId)) {
            throw new UserExistsException("User has already Followed the creator!");
        }

        User userQueued = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User Not Found!"));

        if (!queuedUsername.equals(userQueued.getUsername())) {
            throw new BadCredentialsException("User queued for Adding is not the same as the Active user!");
        }

        User creatorQueued = userRepo.findByUserId(creatorId).orElseThrow(() -> new UserNotFoundException("Creator Not Found!"));


        return followedCreatorRepo.save(
                new FollowedCreator(userQueued, creatorQueued)
        );
    }

    public FollowedCreator removeFollowedCreator(UUID userId, UUID creatorId, String accessToken) {
        if (!jwtService.validateToken(accessToken)) {
            throw new BadCredentialsException("Bad Credentials");
        };

        String queuedUsername = jwtService.getUsername(accessToken);

        FollowedCreator queuedFollowedItem = followedCreatorRepo.findByUser_UserIdAndCreator_UserId(userId, creatorId).orElseThrow(() -> {
            logger.error("No Follow data found in database for userId: {}, and creatorId: {}", userId, creatorId);
            return new UserNotFoundException("No Follow data found in database for userId: " + userId +  ", and creatorId: " + creatorId);
        });

        if (!queuedUsername.equals(queuedFollowedItem.getUser().getUsername())) {
            throw new BadCredentialsException("User queued for Deletion is not the same as the Active user!");
        }

        try {
            followedCreatorRepo.deleteByUser_UserIdAndCreator_UserId(userId, creatorId);
        } catch (DataAccessException e) {
            throw new DatabaseCRUDException("Database error during data deletion: " + e.getMessage());
        }

        return queuedFollowedItem;
    }

    private AuthResponse JwToken(User user) {
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

    public AuthResponse refreshJwToken(
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
