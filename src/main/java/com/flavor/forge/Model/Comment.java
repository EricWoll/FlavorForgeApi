package com.flavor.forge.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID commentId;
    private UUID userId;
    private UUID attachedId;
    private String commentText;

    public Comment(UUID userId, UUID attachedId, String commentText) {
        this.userId = userId;
        this.attachedId = attachedId;
        this.commentText = commentText;
    }
}
