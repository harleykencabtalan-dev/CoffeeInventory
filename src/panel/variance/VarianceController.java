package panel.variance;

import model.DatabaseManager;
import model.Ingredient;
import model.InventoryManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VarianceController {

    private final InventoryManager im;

    // Audit entries: ingredient → physical stock entered by user
    private final Map<Ingredient, Double> auditEntries = new LinkedHashMap<>();
    private boolean locked = false;

    public VarianceController(InventoryManager im) {
        this.im = im;
    }

    // ─── Ingredients ─────────────────────────────────────────────────────────
    public List<Ingredient> getIngredients() {
        return im.getIngredients();
    }

    public void reload() {
        im.reload();
    }

    // ─── Audit entries ────────────────────────────────────────────────────────
    public boolean isLocked() {
        return locked;
    }

    public void setEntry(Ingredient ing, double physicalStock) {
        if (!locked) auditEntries.put(ing, physicalStock);
    }

    public void removeEntry(Ingredient ing) {
        if (!locked) auditEntries.remove(ing);
    }

    public void clearEntries() {
        if (!locked) auditEntries.clear();
    }

    public Map<Ingredient, Double> getEntries() {
        return auditEntries;
    }

    // ─── Variance calculation ─────────────────────────────────────────────────
    public double getVariance(Ingredient ing) {
        Double physical = auditEntries.get(ing);
        if (physical == null) return 0;
        return physical - ing.getTheoreticalStock();
    }

    public String getStatus(Ingredient ing) {
        double variance = getVariance(ing);
        if (variance > 0)                   return "Surplus";           // more stock than expected
        if (Math.abs(variance) < 0.001)     return "Normal";
        double threshold = ing.getLowStockThreshold();
        double pct = threshold > 0 ? Math.abs(variance) / threshold : 1.0;
        if (pct < 0.1)                      return "Normal";
        if (pct < 0.3)                      return "Minor Shortage";
        return "Critical Shortage";
    }

    // ─── Lock / finalize ──────────────────────────────────────────────────────
    /** Finalizes the audit. Returns error string or null on success. */
    public String finalizeAudit() {
        if (auditEntries.isEmpty()) return "No audit entries to finalize.";
        locked = true;
        // Persist each entry to DB
        for (Map.Entry<Ingredient, Double> e : auditEntries.entrySet()) {
            DatabaseManager.saveAudit(e.getKey(), e.getValue(), getVariance(e.getKey()), getStatus(e.getKey()));
        }
        return null;
    }

    public void unlock() {
        locked = false;
    }

    // ─── History ─────────────────────────────────────────────────────────────
    public List<String> loadAuditHistory() {
        return DatabaseManager.loadAuditHistory();
    }
}