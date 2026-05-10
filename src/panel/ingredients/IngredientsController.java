package panel.ingredients;

import model.DatabaseManager;
import model.Ingredient;
import model.InventoryManager;

import java.util.List;

public class IngredientsController {

    private final InventoryManager im;

    public IngredientsController(InventoryManager im) {
        this.im = im;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public List<Ingredient> getIngredients() {
        return im.getIngredients();
    }

    public double getStock(Ingredient ing) {
        return im.getStock(ing);
    }

    // ─── Add Ingredient ───────────────────────────────────────────────────────

    /**
     * Validates inputs then persists to DB and reloads manager.
     * Returns an error string on failure, null on success.
     */
    public String addIngredient(String name, String unit,
                                double initialStock, double threshold) {
        name = name.trim();
        unit = unit.trim();

        if (name.isEmpty())   return "Name cannot be empty.";
        if (unit.isEmpty())   return "Unit cannot be empty.";
        if (initialStock < 0) return "Initial stock cannot be negative.";
        if (threshold < 0)    return "Threshold cannot be negative.";

        // Check for duplicate name
        for (Ingredient ing : im.getIngredients()) {
            if (ing.getDisplayName().equalsIgnoreCase(name))
                return "An ingredient named \"" + name + "\" already exists.";
        }

        DatabaseManager.addIngredient(name, unit, threshold, initialStock);
        im.reload();
        return null;
    }

    // ─── Edit Ingredient ──────────────────────────────────────────────────────

    /**
     * Since Ingredient is immutable and there is no updateIngredient endpoint,
     * we remove the old record and re-add it with the new name/unit/threshold,
     * preserving the current stock value.
     * Returns an error string on failure, null on success.
     */
    public String editIngredient(Ingredient target, String newName,
                                 String newUnit, double newThreshold) {
        newName = newName.trim();
        newUnit = newUnit.trim();

        if (newName.isEmpty()) return "Name cannot be empty.";
        if (newUnit.isEmpty()) return "Unit cannot be empty.";
        if (newThreshold < 0)  return "Threshold cannot be negative.";

        // Check for duplicate name (excluding the ingredient being edited)
        for (Ingredient ing : im.getIngredients()) {
            if (ing.getId() != target.getId()
                    && ing.getDisplayName().equalsIgnoreCase(newName))
                return "An ingredient named \"" + newName + "\" already exists.";
        }

        double currentStock = im.getStock(target);

        // Remove old, re-add with new values preserving stock
        DatabaseManager.removeIngredient(target.getId());
        DatabaseManager.addIngredient(newName, newUnit, newThreshold, currentStock);
        im.reload();
        return null;
    }

    // ─── Controls: update threshold only ─────────────────────────────────────

    /**
     * Updates only the low-stock threshold for an ingredient.
     * Implemented as remove + re-add (same pattern as edit) since there is
     * no dedicated update endpoint.
     */
    public String updateThreshold(Ingredient ing, double newThreshold) {
        if (newThreshold < 0) return "Threshold cannot be negative.";

        double currentStock = im.getStock(ing);
        DatabaseManager.removeIngredient(ing.getId());
        DatabaseManager.addIngredient(
            ing.getDisplayName(), ing.getUnit(), newThreshold, currentStock);
        im.reload();
        return null;
    }

    // ─── Remove Ingredient ────────────────────────────────────────────────────

    public void removeIngredient(Ingredient ing) {
        DatabaseManager.removeIngredient(ing.getId());
        im.reload();
    }
}