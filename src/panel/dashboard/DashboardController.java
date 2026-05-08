package panel.dashboard;

import model.InventoryManager;
import model.Ingredient;
import model.CoffeeType;
import model.ProductionRecord;
import model.RefillRecord;

import java.util.List;

public class DashboardController {
    private final InventoryManager inventoryManager;

    public DashboardController(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public int getTotalIngredients() {
        return inventoryManager.getIngredients().size();
    }

    public int getCriticalCount() {
        return inventoryManager.getLowStockList().size();
    }

    public int getOutOfStockCount() {
        int count = 0;
        for (Ingredient ing : inventoryManager.getIngredients()) {
            if (inventoryManager.getStock(ing) <= 0) count++;
        }
        return count;
    }

    public int getProductionToday() {
        return inventoryManager.getProductionLog().size();
    }

    // ─── Inventory levels for bar chart ───────────────────────────────────────
    public List<Ingredient> getIngredients() {
        return inventoryManager.getIngredients();
    }

    public double getStock(Ingredient ing) {
        return inventoryManager.getStock(ing);
    }

    // Status: "SUFFICIENT", "CRITICAL", "OUT OF STOCK"
    public String getStockStatus(Ingredient ing) {
        double stock = inventoryManager.getStock(ing);
        if (stock <= 0)                                return "OUT OF STOCK";
        if (stock < ing.getLowStockThreshold())        return "CRITICAL";
        return "SUFFICIENT";
    }

    // Ratio 0.0–1.0 for the progress bar
    public double getStockRatio(Ingredient ing) {
        double stock     = inventoryManager.getStock(ing);
        double threshold = ing.getLowStockThreshold();
        double max       = Math.max(threshold * 3, stock); // scale bar relative to 3× threshold
        return max <= 0 ? 0 : Math.min(1.0, stock / max);
    }

    // ─── Production capability map ────────────────────────────────────────────
    public List<CoffeeType> getCoffeeTypes() {
        return inventoryManager.getCoffeeTypes();
    }

    public int maxProducible(CoffeeType ct, double sizeMultiplier) {
        return inventoryManager.maxProducible(ct, sizeMultiplier);
    }

    // ─── Alerts ───────────────────────────────────────────────────────────────
    public List<String> getAlerts() {
        return inventoryManager.getAlerts();
    }

    // ─── Recent activities (production + refill merged, latest first) ─────────
    public List<String> getRecentActivities(int limit) {
        List<String> activities = new java.util.ArrayList<>();

        // Production log
        java.util.Stack<ProductionRecord> prodLog = inventoryManager.getProductionLog();
        java.util.List<ProductionRecord> prodList = new java.util.ArrayList<>(prodLog);
        java.util.Collections.reverse(prodList);
        for (ProductionRecord rec : prodList) {
            String ts = rec.getTimestamp() != null
                ? rec.getTimestamp().toString().substring(0, 16).replace("T", " ")
                : "";
            activities.add("PROD|Produced " + rec.getQuantity() + "× " + rec.getCoffeeName() + "|" + ts);
        }

        // Refill log
        java.util.List<RefillRecord> refillList = new java.util.ArrayList<>(inventoryManager.getRefillLog());
        java.util.Collections.reverse(refillList);
        for (RefillRecord rec : refillList) {
            String ts = rec.getTimestamp() != null
                ? rec.getTimestamp().toString().substring(0, 16).replace("T", " ")
                : "";
            activities.add("REFILL|Refilled " + rec.getIngredient().getDisplayName()
                + ": +" + String.format("%.0f", rec.getAmount()) + rec.getIngredient().getUnit()
                + "|" + ts);
        }

        // Variance logs
        for (String v : inventoryManager.getVarianceLogs()) {
            activities.add("VARIANCE|" + v + "|");
        }

        // Sort by timestamp descending (simple string sort works for ISO timestamps)
        activities.sort((a, b) -> {
            String tsA = a.split("\\|").length > 2 ? a.split("\\|")[2] : "";
            String tsB = b.split("\\|").length > 2 ? b.split("\\|")[2] : "";
            return tsB.compareTo(tsA);
        });

        return activities.subList(0, Math.min(limit, activities.size()));
    }

    // ─── Latest variance audit summary ────────────────────────────────────────
    public String getLatestVarianceSummary() {
        List<String> logs = inventoryManager.getVarianceLogs();
        if (logs.isEmpty()) return "No variance audit on record.";
        return logs.get(logs.size() - 1);
    }

    public InventoryManager getManager() {
        return inventoryManager;
    }
}