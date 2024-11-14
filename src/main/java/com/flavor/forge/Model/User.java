package com.flavor.forge.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Data
@Document(collection = "user")
public class User implements UserDetails {

    @MongoId
    private ObjectId id;
    private String userId;
    private String username;
    private String email;
    private String password;
    private String imageId;
    private int followerCount;
    private String aboutText;
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
        this.userId = UUID.randomUUID().toString();
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role.getAuthorities();
    }
}
