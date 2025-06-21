package com.flavor.forge.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "comment")
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comment_id")
    private UUID commentId;

    @NotNull
    @Column(name = "user_id")
    private String userId;

    @NotNull
    @Column(name = "attached_id")
    private UUID attachedId;

    @NotNull
    @NotEmpty
    @Size(min = 1, message = "Comment must have text!")
    @Column(name = "comment_text")
    private String commentText;

    public Comment(String userId, UUID attachedId, String commentText) {
        this.userId = userId;
        this.attachedId = attachedId;
        this.commentText = commentText;
    }
}
