package com.flavor.forge.Model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Ingredient {
    private String ingredientName;
    private String amount;

    public Ingredient(String name, String amount) {
        this.ingredientName = name;
        this.amount = amount;
    }
}
