package panel.inventory;

import model.Ingredient;
import model.InventoryManager;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryController {

    private final InventoryManager im;

    public InventoryController(InventoryManager im) {
        this.im = im;
    }

    // ─── Data ─────────────────────────────────────────────────────────────────
    public List<Ingredient> getIngredients() {
        return im.getIngredients();
    }

    public double getStock(Ingredient ing) {
        return im.getStock(ing);
    }

    public void reload() {
        im.reload();
    }

    // ─── Status ───────────────────────────────────────────────────────────────
    public String getStatus(Ingredient ing) {
        double stock = im.getStock(ing);
        if (stock <= 0)                          return "OUT";
        if (stock < ing.getLowStockThreshold())  return "LOW";
        return "OK";
    }

    // ─── Search / filter ──────────────────────────────────────────────────────
    public List<Ingredient> search(String query) {
        if (query == null || query.isBlank()) return im.getIngredients();
        String q = query.toLowerCase();
        return im.getIngredients().stream()
                .filter(i -> i.getDisplayName().toLowerCase().contains(q)
                          || i.getUnit().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // ─── Toggle (show only low stock) ─────────────────────────────────────────
    public List<Ingredient> getLowStock() {
        return im.getLowStockList();
    }

    // ─── Adjust stock (add positive, deduct negative) ─────────────────────────
    public String addStock(Ingredient ing, double amount, String reason) {
        if (amount <= 0) return "Amount must be greater than 0.";
        if (reason == null || reason.isBlank()) return "Reason is required.";
        im.adjustStock(ing, amount, reason);
        return null; // null = success
    }

    public String deductStock(Ingredient ing, double amount, String reason) {
        if (amount <= 0) return "Amount must be greater than 0.";
        if (reason == null || reason.isBlank()) return "Reason is required.";
        double current = im.getStock(ing);
        if (amount > current) return String.format(
                "Cannot deduct %.2f — only %.2f %s available.", amount, current, ing.getUnit());
        im.adjustStock(ing, -amount, reason);
        return null;
    }

    public String setExactStock(Ingredient ing, double value, String reason) {
        if (value < 0) return "Stock value cannot be negative.";
        if (reason == null || reason.isBlank()) return "Reason is required.";
        im.setStock(ing, value, reason);
        return null;
    }
}