package com.flavor.forge.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Data
@Entity
@Table(name = "liked_recipe", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "recipe_id"})
})
@NoArgsConstructor
public class LikedRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "liked_id")
    private UUID likedId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public LikedRecipe(User user, Recipe recipe) {
        this.user = user;
        this.recipe = recipe;
    }
}
