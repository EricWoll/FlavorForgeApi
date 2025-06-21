package com.flavor.forge.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id"),
        @UniqueConstraint(columnNames = "username")
})
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Version
    private Long version; // Optimistic locking version field

    @NotEmpty(message = "Username cannot be empty!")
    @Size(min = 5, message = "Username must have at least 5 characters!")
    private String username;

    @NotEmpty
    @Column(name = "image_id")
    private String imageId;

    @NotNull
    @Min(0)
    @Column(name = "follower_count")
    private int followerCount;

    @Column(name = "about_text")
    private String aboutText;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ERole role = ERole.FREE;

    public User(
            String userId,
            String username,
            String imageId,
            int followerCount,
            String aboutText,
            ERole role
    ) {
        this.userId = userId;
        this.username = username;
        this.imageId = imageId;
        this.followerCount = followerCount;
        this.aboutText = aboutText;
        this.role = role;
    }

    public void addFollower() {
        this.followerCount++;
    }

    public void removeFollower() {
        this.followerCount--;
    }
}
