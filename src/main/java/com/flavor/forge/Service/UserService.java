package com.flavor.forge.Service;

import com.flavor.forge.Exception.CustomExceptions.DatabaseCRUDException;
import com.flavor.forge.Exception.CustomExceptions.UserExistsException;
import com.flavor.forge.Exception.CustomExceptions.UserNotFoundException;
import com.flavor.forge.Model.DTO.*;
import com.flavor.forge.Model.ERole;
import com.flavor.forge.Model.FollowedCreator;
import com.flavor.forge.Model.ProcessedWebHookEvent;
import com.flavor.forge.Model.User;
import com.flavor.forge.Repo.FollowedCreatorRepo;
import com.flavor.forge.Repo.ProcessedWebHookEventRepository;
import com.flavor.forge.Repo.UserRepo;
import com.flavor.forge.Security.Clerk.ClerkService;
import jakarta.transaction.Transactional;
import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowedCreatorRepo followedCreatorRepo;

    @Autowired
    private ProcessedWebHookEventRepository processedWebHookEventRepository;

    @Autowired
    private ClerkService clerkService;

    @Value("${SEARCH_LIMIT}")
    private short searchLimit;

    @Value("${forge.app.noImage}")
    private String noImageId;

    private BCryptPasswordEncoder bcpEncoder = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 12);

    public PublicUserDTO findSinglePublicUser(String creatorId, String userId) {
        Object[] user = (Object[]) userRepo.findPublicUserByUserId(creatorId, userId).orElseThrow(() -> {
            logger.error("User not found with userId of: {}.\"", userId);
            return new UserNotFoundException("User not found with userId of: " + userId);
        });

        return sqlQueryObjectMapToPublicUserDTO(user);
    }

    public PrivateUserDTO findSinglePrivateUser(String userId, String accessToken) {

        Object[] user = (Object[]) userRepo.findPrivateUserByUserId(userId).orElseThrow(() -> {
            logger.error("User not found with userId of: {}.\"", userId);
            return new UserNotFoundException("User not found with userId of: " + userId);
        });

        PrivateUserDTO privateUser = sqlQueryObjectMapToPrivateUserDTO(user);

        return privateUser;
    }

    @Transactional
    public User createUser(User user, String eventId) {
        // Acquire a lock for this user (or you could key by eventId if that suits your use case better)
        logger.info("Starting to create User!");
        Object lock = locks.computeIfAbsent(user.getUserId(), k -> new Object());

        synchronized (lock) {
            try {

                // Early idempotency check: If this event has already been processed, return the existing user.
                if (processedWebHookEventRepository.findByEventId(eventId).isPresent()) {
                    return userRepo.findByUserId(user.getUserId())
                            .orElseThrow(() -> new RuntimeException("User already created but not found"));
                }

                // Check if user exists by userId.
                Optional<User> existingUserById = userRepo.findByUserId(user.getUserId());
                if (existingUserById.isPresent()) {
                    processedWebHookEventRepository.save(new ProcessedWebHookEvent(eventId));
                    return existingUserById.get();
                }

                // Check by username to avoid duplicates.
                Optional<User> existingUserByUsername = userRepo.findByUsername(user.getUsername());
                if (existingUserByUsername.isPresent()) {
                    throw new RuntimeException("Username already exists");
                }

                // Fetch role from Clerk.
                ERole userRole;
                try {
                    userRole = clerkService.fetchUserRoleFromClerk(user.getUserId());
                } catch (Exception e) {
                    logger.error("Failed to fetch user role from Clerk: " + e.getMessage());
                    userRole = ERole.FREE;
                }

                // Set a default image if needed.
                if (user.getImageId() == null) {
                    user.setImageId(noImageId);
                }

                // Build new user instance from provided data.
                User newUser = new User(
                        user.getUserId(),
                        user.getUsername(),
                        user.getImageId(),
                        0,
                        "",
                        userRole
                );

                // Attempt to save the user. Since we're inside a lock for this user,
                // there should be no concurrent modification for this key.
                User savedUser = userRepo.save(newUser);

                // Record the processed event to ensure idempotency in future attempts.
                processedWebHookEventRepository.save(new ProcessedWebHookEvent(eventId));

                return savedUser;
            } finally {
                // Remove the lock once processing is complete.
                locks.remove(user.getUserId());
            }
        }
    }

    @Transactional
    public User updateUser(String userId, User user) {
        User foundUser = userRepo.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.error("User with userId of \"{}\" is missing some content and cannot be updated!", userId);
                    return new UserNotFoundException("Some User information is missing!");
                });

        foundUser.setUsername(user.getUsername());
        foundUser.setImageId(user.getImageId());
        foundUser.setAboutText(user.getAboutText());

        userRepo.save(foundUser);
        return foundUser;
    }

    @Transactional
    public User deleteUser(String userId) {

        User foundUser = userRepo.findByUserId(userId)
                .orElseThrow(()-> {
                    logger.error("User does not exists with userId of \"{}\" and cannot be updated!", userId);
                    return new UserNotFoundException("User Does Not Exist!");
                });

        userRepo.deleteByUserId(userId);
        return foundUser;
    }

    public List<FollowedCreatorDTO> findFollowedCreators(String userId, String accessToken, int listOffset) {

        List<Object[]> creatorList = followedCreatorRepo.findAllByUserId(userId, searchLimit, listOffset);

        return creatorList.stream().map(this::sqlQueryObjectMapToFollowedCreatorDTO).toList();
    }

    public List<FollowedCreatorDTO> findFollowedCreatorsWithSearch(String userId, String searchString, String accessToken, int listOffset) {

        List<Object[]> creatorList = followedCreatorRepo.searchWithString(userId, searchString, searchLimit, listOffset);

        return creatorList.stream().map(this::sqlQueryObjectMapToFollowedCreatorDTO).toList();
    }

    public FollowedCreator addFollowedCreator(String userId, String creatorId, String accessToken) {

        if (followedCreatorRepo.existsByUser_UserIdAndCreator_UserId(userId, creatorId)) {
            throw new UserExistsException("User has already Followed the creator!");
        }

        User userQueued = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User Not Found!"));

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

    @Transactional
    public FollowedCreator removeFollowedCreator(String userId, String creatorId, String accessToken) {
        FollowedCreator followedEntry = followedCreatorRepo
                .findByUser_UserIdAndCreator_UserId(userId, creatorId)
                .orElseThrow(() -> {
                    logger.warn("No follow data found for userId: {}, creatorId: {}", userId, creatorId);
                    return new UserNotFoundException("No follow data found for userId: " + userId + ", creatorId: " + creatorId);
                });

        User creator = userRepo.findByUserId(creatorId)
                .orElseThrow(() -> new UserNotFoundException("Creator not found for ID: " + creatorId));

        if (creator.getFollowerCount() > 0) {
            creator.setFollowerCount(creator.getFollowerCount() - 1);
        }

        try {
            followedCreatorRepo.deleteByUser_UserIdAndCreator_UserId(userId, creatorId);
            userRepo.save(creator);
        } catch (DataAccessException e) {
            throw new DatabaseCRUDException("Database error during unfollow operation: " + e.getMessage(), e);
        }

        return followedEntry;
    }

    // Mapper Utility for SQL Queries to User
    private PublicUserDTO sqlQueryObjectMapToPublicUserDTO(Object[] sqlQueryResult) {
        return PublicUserDTO.builder()
                .userId((String) sqlQueryResult[0])
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
                .userId((String) sqlQueryResult[0])
                .username((String) sqlQueryResult[1])
                .email((String) sqlQueryResult[2])
                .imageId((String) sqlQueryResult[3])
                .aboutText((String) sqlQueryResult[4])
                .build();
    }

    // Mapper Utility for SQL Queries to Recipes
    private FollowedCreatorDTO sqlQueryObjectMapToFollowedCreatorDTO(Object[] sqlQueryResult) {
        return FollowedCreatorDTO.builder()
                .userId((String) sqlQueryResult[0])
                .username((String) sqlQueryResult[1])
                .imageId((String) sqlQueryResult[2])
                .followerCount((Integer) sqlQueryResult[3])
                .aboutText((String) sqlQueryResult[4])
                .isFollowed(true)
                .build();
    }

}
