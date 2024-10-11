package com.flavor.forge.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User implements UserDetails {

    @MongoId
    private ObjectId Id;
    private String Username;
    private String Email;
    private String Password;
    private String ImageId;
    private int FollowerCount;
    private String AboutText;
    private ERole Role;

    public User(
            String username,
            String email,
            String password,
            String imageId,
            int followerCount,
            String aboutText,
            ERole role
    ) {
        this.Username = username;
        this.Email = email;
        this.Password = password;
        this.ImageId = imageId;
        this.FollowerCount = followerCount;
        this.AboutText = aboutText;
        this.Role = role;
    }

    public void addFollower() {
        FollowerCount++;
    }

    public void removeFollower() {
        FollowerCount--;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Role.getAuthorities();
    }
}
