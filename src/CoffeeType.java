import java.util.*;

public class CoffeeType {

    private final int                     id;
    private final String                  name;
    private final Map<Ingredient, Double> recipe;

    public CoffeeType(int id, String name, Map<Ingredient, Double> recipe) {
        this.id     = id;
        this.name   = name;
        this.recipe = Collections.unmodifiableMap(recipe);
    }

    public int                     getId()          { return id;     }
    public String                  getDisplayName() { return name;   }
    public Map<Ingredient, Double> getRecipe()      { return recipe; }

    @Override
    public String toString() { return name; }
}