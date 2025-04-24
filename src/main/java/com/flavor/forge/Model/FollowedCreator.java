package com.flavor.forge.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "followed_creator")
public class FollowedCreator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "followed_id")
    private UUID followedId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    public FollowedCreator(User user, User creator) {
        this.user = user;
        this.creator = creator;
    }
}
