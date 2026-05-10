package model;

import java.util.*;

public class CoffeeType {

    private final int                     id;
    private final String                  name;
    private final String                  category;
    private final Map<Ingredient, Double> recipe;

    public CoffeeType(int id, String name, String category, Map<Ingredient, Double> recipe) {
        this.id       = id;
        this.name     = name;
        this.category = category != null ? category : "";
        this.recipe   = Collections.unmodifiableMap(recipe);
    }

    // Backwards-compatible constructor (no category)
    public CoffeeType(int id, String name, Map<Ingredient, Double> recipe) {
        this(id, name, "", recipe);
    }

    public int                     getId()          { return id;       }
    public String                  getDisplayName() { return name;     }
    public String                  getCategory()    { return category; }
    public Map<Ingredient, Double> getRecipe()      { return recipe;   }

    @Override
    public String toString() { return name; }
}