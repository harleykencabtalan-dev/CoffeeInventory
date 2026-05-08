package panel.production;

import model.CoffeeType;
import model.Ingredient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents one independent coffee cup order with its own
 * size, temperature, and optionally a customised recipe.
 */
public class OrderItem {

    public CoffeeType coffeeType;
    public String     size         = "Medium";
    public String     temp         = "Hot";
    public boolean    isCustomized = false;
    public Map<Ingredient, Double> recipe = new LinkedHashMap<>();

    public OrderItem(CoffeeType ct) {
        this.coffeeType = ct;
        recipe.putAll(ct.getRecipe());
    }

    /** Label shown in the coffee-selector dropdown inside Step 2. */
    public String label(int index) {
        return "Coffee " + (index + 1) + " — " + coffeeType.getDisplayName()
                + "  [" + size + ", " + temp + (isCustomized ? ", Custom" : "") + "]";
    }
}