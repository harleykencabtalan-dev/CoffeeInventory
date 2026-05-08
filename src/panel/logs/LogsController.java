package panel.logs;

import model.InventoryManager;
import model.ProductionRecord;
import model.RefillRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogsController {

    public enum Filter { TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, ALL }

    private final InventoryManager im;

    public LogsController(InventoryManager im) {
        this.im = im;
    }

    // ─── Production Logs ──────────────────────────────────────────────────────
    public List<ProductionRecord> getProductionLogs(Filter filter) {
        List<ProductionRecord> all = new ArrayList<>(im.getProductionLog());
        Collections.reverse(all);
        return filterProduction(all, filter);
    }

    private List<ProductionRecord> filterProduction(List<ProductionRecord> records, Filter filter) {
        if (filter == Filter.ALL) return records;
        List<ProductionRecord> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (ProductionRecord rec : records) {
            LocalDate date = rec.getTimestamp().toLocalDate();
            if (matchesFilter(date, now, filter)) result.add(rec);
        }
        return result;
    }

    // ─── Refill Logs ──────────────────────────────────────────────────────────
    public List<RefillRecord> getRefillLogs(Filter filter) {
        List<RefillRecord> all = new ArrayList<>(im.getRefillLog());
        Collections.reverse(all);
        return filterRefill(all, filter);
    }

    private List<RefillRecord> filterRefill(List<RefillRecord> records, Filter filter) {
        if (filter == Filter.ALL) return records;
        List<RefillRecord> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (RefillRecord rec : records) {
            LocalDate date = rec.getTimestamp().toLocalDate();
            if (matchesFilter(date, now, filter)) result.add(rec);
        }
        return result;
    }

    // ─── Variance Logs ────────────────────────────────────────────────────────
    public List<String> getVarianceLogs(Filter filter) {
        // Returns variance audit entries from InventoryManager
        List<String> all = new ArrayList<>(im.getVarianceLogs());
        Collections.reverse(all);
        return all; 
    }

    // ─── Filter Helper ────────────────────────────────────────────────────────
    private boolean matchesFilter(LocalDate date, LocalDate now, Filter filter) {
        return switch (filter) {
            case TODAY     -> date.equals(now);
            case YESTERDAY -> date.equals(now.minusDays(1));
            case THIS_WEEK -> !date.isBefore(now.minusDays(now.getDayOfWeek().getValue() - 1));
            case THIS_MONTH -> date.getMonth() == now.getMonth() && date.getYear() == now.getYear();
            default        -> true;
        };
    }
}