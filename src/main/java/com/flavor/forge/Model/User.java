package com.flavor.forge.Model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Data
@Entity
@Table(name = "\"user\"")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    private String username;
    private String email;
    private String password;
    private String imageId;
    private int followerCount;
    private String aboutText;

    @Column(name="password_resetId", columnDefinition = "DEFAULT NULL", nullable = true)
    private UUID passwordResetId;

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
