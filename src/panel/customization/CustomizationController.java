package panel.customization;

import model.CoffeeType;
import model.DatabaseManager;
import model.Ingredient;
import model.InventoryManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomizationController {

    private final InventoryManager im;

    public CustomizationController(InventoryManager im) {
        this.im = im;
    }

    // ─── Data ─────────────────────────────────────────────────────────────────
    public List<CoffeeType> getCoffeeTypes() {
        return im.getCoffeeTypes();
    }

    public List<Ingredient> getIngredients() {
        return im.getIngredients();
    }

    public List<String> getCategories() {
    return DatabaseManager.loadCategories();
}

public void addCategory(String name) {
    DatabaseManager.addCategory(name);
}

public void removeCategory(String name) {
    DatabaseManager.removeCategory(name);
}

    // ─── Add new coffee type ──────────────────────────────────────────────────
    // Returns error string or null on success
    public String addCoffeeType(String name, String category,
                                Map<Ingredient, Double> recipe) {
        if (name == null || name.isBlank())     return "Name is required.";
        if (category == null || category.isBlank()) return "Category is required.";
        if (recipe.isEmpty())                   return "At least one ingredient is required.";

        // Check for duplicate name
        for (CoffeeType ct : im.getCoffeeTypes()) {
            if (ct.getDisplayName().equalsIgnoreCase(name.trim()))
                return "A coffee type named \"" + name + "\" already exists.";
        }

        // Build id-keyed recipe for DB
        Map<Integer, Double> dbRecipe = new LinkedHashMap<>();
        for (Map.Entry<Ingredient, Double> e : recipe.entrySet()) {
            dbRecipe.put(e.getKey().getId(), e.getValue());
        }

        DatabaseManager.addCoffeeType(name.trim(), category.trim(), dbRecipe);
        im.reload(); // refresh in-memory list
        return null;
    }

    // ─── Update coffee type name + category ───────────────────────────────────
    public String updateCoffeeType(CoffeeType ct, String newName, String newCategory) {
        if (newName == null || newName.isBlank())         return "Name is required.";
        if (newCategory == null || newCategory.isBlank()) return "Category is required.";

        DatabaseManager.updateCoffeeType(ct.getId(), newName.trim(), newCategory.trim());
        im.reload();
        return null;
    }

    // ─── Delete coffee type ───────────────────────────────────────────────────
    public void deleteCoffeeType(CoffeeType ct) {
        DatabaseManager.removeCoffeeType(ct.getId());
        im.reload();
    }

    // ─── Recipe: add ingredient ───────────────────────────────────────────────
    public String addIngredientToRecipe(CoffeeType ct, Ingredient ing, double amount) {
        if (amount <= 0) return "Amount must be greater than 0.";
        if (ct.getRecipe().containsKey(ing))
            return ing.getDisplayName() + " is already in the recipe. Edit its amount instead.";
        DatabaseManager.addRecipeRow(ct.getId(), ing.getId(), amount);
        im.reload();
        return null;
    }

    // ─── Recipe: update ingredient amount ─────────────────────────────────────
    public String updateIngredientAmount(CoffeeType ct, Ingredient ing, double amount) {
        if (amount <= 0) return "Amount must be greater than 0.";
        DatabaseManager.updateRecipeRow(ct.getId(), ing.getId(), amount);
        im.reload();
        return null;
    }

    // ─── Recipe: remove ingredient ────────────────────────────────────────────
    public String removeIngredientFromRecipe(CoffeeType ct, Ingredient ing) {
        if (ct.getRecipe().size() <= 1)
            return "Cannot remove the last ingredient from a recipe.";
        DatabaseManager.removeRecipeRow(ct.getId(), ing.getId());
        im.reload();
        return null;
    }

    // ─── Find live ingredient by ID (avoids stale ref) ────────────────────────
    public Ingredient findIngredientById(int id) {
        for (Ingredient ing : im.getIngredients()) {
            if (ing.getId() == id) return ing;
        }
        return null;
    }
}