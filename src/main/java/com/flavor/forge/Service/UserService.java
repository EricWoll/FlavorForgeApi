package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.DatabaseCRUDException;
import com.flavor.forge.Exception.CustomExceptions.UserExistsException;
import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.DTO.*;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
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

    @Value("${SEARCH_LIMIT}")
    private short searchLimit;

    @Value("${forge.app.noImage}")
    private String noImageId;

    @Autowired
    private AuthenticationManager authManager;

    private BCryptPasswordEncoder bcpEncoder = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 12);

    public PublicUserDTO findSinglePublicUser(UUID creatorId, UUID userId) {
        Object[] user = (Object[]) userRepo.findPublicUserByUserId(creatorId, userId).orElseThrow(() -> {
            logger.error("User not found with userId of: {}.\"", userId);
            return new UserNotFoundException("User not found with userId of: " + userId);
        });

        return sqlQueryObjectMapToPublicUserDTO(user);
    }

    public PrivateUserDTO findSinglPrivateUser(UUID userId, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        Object[] user = (Object[]) userRepo.findPrivateUserByUserId(userId).orElseThrow(() -> {
            logger.error("User not found with userId of: {}.\"", userId);
            return new UserNotFoundException("User not found with userId of: " + userId);
        });

        PrivateUserDTO privateUser = sqlQueryObjectMapToPrivateUserDTO(user);

        jwtService.validateAccessTokenAgainstFoundUsername(accessToken, privateUser.getUsername());

        return privateUser;
    }

    public AuthDTO createUser(User user) {
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
                        bcpEncoder.encode(user.getPassword()),
                        user.getImageId(),
                        0,
                        "",
                        ERole.FREE
                )
        );
        return JwToken(createdUser);
    }

    public User updateUser(UUID userId, User user, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        User foundUser = userRepo.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("User with userId of \"{}\" is missing some content and cannot be updated!", userId);
                    return new UserNotFoundException("Some User information is missing!");
                });

        jwtService.validateAccessTokenAgainstFoundUsername(accessToken, foundUser.getUsername());

        foundUser.setUsername(user.getUsername());
        foundUser.setEmail(user.getEmail());
        foundUser.setImageId(user.getImageId());
        foundUser.setAboutText(user.getAboutText());

        userRepo.save(foundUser);
        return foundUser;
    }

    public User deleteUser(UUID userId, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        User foundUser = userRepo.findByUserId(userId)
                .orElseThrow(()-> {
                    logger.error("User does not exists with userId of \"{}\" and cannot be updated!", userId);
                    return new UserNotFoundException("User Does Not Exist!");
                });

        jwtService.validateAccessTokenAgainstFoundUsername(accessToken, foundUser.getUsername());

        userRepo.deleteByUserId(userId);
        return foundUser;
    }

    public AuthDTO login(User user) {
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

    public List<FollowedCreatorDTO> findFollowedCreators(UUID userId, String accessToken, int listOffset) {
        jwtService.validateAccessTokenCredentials(accessToken);

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, userId);

        List<Object[]> creatorList = followedCreatorRepo.findAllByUserId(userId, searchLimit, listOffset);

        return creatorList.stream().map(this::sqlQueryObjectMapToFollowedCreatorDTO).toList();
    }

    public List<FollowedCreatorDTO> findFollowedCreatorsWithSearch(UUID userId, String searchString, String accessToken, int listOffset) {
        jwtService.validateAccessTokenCredentials(accessToken);

        jwtService.validateAccessTokenAgainstFoundUserId(accessToken, userId);

        List<Object[]> creatorList = followedCreatorRepo.searchWithString(userId, searchString, searchLimit, listOffset);

        return creatorList.stream().map(this::sqlQueryObjectMapToFollowedCreatorDTO).toList();
    }

    public FollowedCreator addFollowedCreator(UUID userId, UUID creatorId, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        if (followedCreatorRepo.existsByUser_UserIdAndCreator_UserId(userId, creatorId)) {
            throw new UserExistsException("User has already Followed the creator!");
        }

        User userQueued = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User Not Found!"));

        jwtService.validateAccessTokenAgainstFoundUsername(accessToken, userQueued.getUsername());

        User creatorQueued = userRepo.findByUserId(creatorId).orElseThrow(() -> new UserNotFoundException("Creator Not Found!"));

        creatorQueued.setFollowerCount(creatorQueued.getFollowerCount() + 1);

        FollowedCreator followedCreator;

        try {
            followedCreator = followedCreatorRepo.save(
                    new FollowedCreator(userQueued, creatorQueued));
            userRepo.save(creatorQueued);

        } catch (DataAccessException e) {
            throw new DatabaseCRUDException("Database error during data deletion: " + e.getMessage());
        }

        return followedCreator;
    }

    public FollowedCreator removeFollowedCreator(UUID userId, UUID creatorId, String accessToken) {
        jwtService.validateAccessTokenCredentials(accessToken);

        FollowedCreator queuedFollowedItem = followedCreatorRepo.findByUser_UserIdAndCreator_UserId(userId, creatorId).orElseThrow(() -> {
            logger.error("No Follow data found in database for userId: {}, and creatorId: {}", userId, creatorId);
            return new UserNotFoundException("No Follow data found in database for userId: " + userId +  ", and creatorId: " + creatorId);
        });

        jwtService.validateAccessTokenAgainstFoundUsername(accessToken, queuedFollowedItem.getUser().getUsername());

        User creatorQueued = userRepo.findByUserId(creatorId).orElseThrow(() -> new UserNotFoundException("Creator Not Found!"));

        if (creatorQueued.getFollowerCount() > 0) {
        creatorQueued.setFollowerCount(creatorQueued.getFollowerCount() - 1);
        }

        try {
            followedCreatorRepo.deleteByUser_UserIdAndCreator_UserId(userId, creatorId);
            userRepo.save(creatorQueued);
        } catch (DataAccessException e) {
            throw new DatabaseCRUDException("Database error during data deletion: " + e.getMessage());
        }

        return queuedFollowedItem;
    }

    private AuthDTO JwToken(User user) {
        String accessToken = jwtService.generateJwtToken(user);
        String refreshToken = jwtService.generateJwtRefreshToken(user);

        return AuthDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .userId(user.getUserId())
                .role(user.getRole())
                .imageId(user.getImageId())
                .build();
    }

    public AuthDTO refreshJwToken(
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

                return AuthDTO.builder()
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

    // Mapper Utility for SQL Queries to User
    private PublicUserDTO sqlQueryObjectMapToPublicUserDTO(Object[] sqlQueryResult) {
        return PublicUserDTO.builder()
                .userId((UUID) sqlQueryResult[0])
                .username((String) sqlQueryResult[1])
                .imageId((String) sqlQueryResult[2])
                .followerCount((Integer) sqlQueryResult[3])
                .aboutText((String) sqlQueryResult[4])
                .isFollowed(sqlQueryResult.length > 5 ? (Boolean) sqlQueryResult[5] : false)
                .build();
    }
    // Mapper Utility for SQL Queries to Private User
    private PrivateUserDTO sqlQueryObjectMapToPrivateUserDTO(Object[] sqlQueryResult) {
        return PrivateUserDTO.builder()
                .userId((UUID) sqlQueryResult[0])
                .username((String) sqlQueryResult[1])
                .email((String) sqlQueryResult[2])
                .imageId((String) sqlQueryResult[3])
                .aboutText((String) sqlQueryResult[4])
                .build();
    }

    // Mapper Utility for SQL Queries to Recipes
    private FollowedCreatorDTO sqlQueryObjectMapToFollowedCreatorDTO(Object[] sqlQueryResult) {
        return FollowedCreatorDTO.builder()
                .userId((UUID) sqlQueryResult[0])
                .username((String) sqlQueryResult[1])
                .imageId((String) sqlQueryResult[2])
                .followerCount((Integer) sqlQueryResult[3])
                .aboutText((String) sqlQueryResult[4])
                .isFollowed(true)
                .build();
    }

}