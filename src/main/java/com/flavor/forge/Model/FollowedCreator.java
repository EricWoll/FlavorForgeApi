package com.flavor.forge.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "followed_creator")
public class FollowedCreator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID followedId;
    private UUID userId;
    private UUID creatorId;

    public FollowedCreator(UUID userId, UUID creatorId) {
        this.userId = userId;
        this.creatorId = creatorId;
    }
}
