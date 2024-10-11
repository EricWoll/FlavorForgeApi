package com.flavor.forge.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "recipe")
public class Recipe {

    @MongoId
    private ObjectId Id;
    private ObjectId UserId;
    private String RecipeName;
    private String RecipeDescription;
    private List<String> Ingredients;
    private List<String> Steps;
    private String ImageId;
    private int LikesCount;

    public void addLike() {
        LikesCount++;
    }

    public void removeLike() {
        LikesCount--;
    }
}
