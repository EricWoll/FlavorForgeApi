package com.flavor.forge.Model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @NotEmpty(message = "Username cannot be empty!")
    @Size(min = 5, message = "Username must have at least 5 characters!")
    private String username;

    @NotNull
    @NotEmpty
    @Email(message = "Invalid email format")
    private String email;

    @NotNull
    @NotEmpty
    @Size(min = 8, message = "Password must have at least 8 characters!")
    private String password;

    @NotEmpty
    @Column(name = "image_id")
    private String imageId;

    @NotNull
    @Min(0)
    @Column(name = "follower_count")
    private int followerCount;

    @Column(name = "about_text")
    private String aboutText;

    @Column(name="password_resetId", nullable = true)
    private UUID passwordResetId;

    @Column(name = "password_reset_date", nullable = true)
    private Date passwordResetDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private ERole role;

    public User(
            String username,
            String email,
            String password,
            String imageId,
            int followerCount,
            String aboutText,
            ERole role
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
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

    @Override
    @JsonDeserialize(contentUsing = GrantedAuthorityDeserializer.class)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role.getAuthorities();
    }
}
