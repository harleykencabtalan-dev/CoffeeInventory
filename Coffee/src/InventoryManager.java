import java.util.*;

public class InventoryManager {

    private List<Ingredient>              ingredients;
    private List<CoffeeType>              coffeeTypes;

    private final Map<Ingredient, Double> currentStock     = new HashMap<>();
    private final Map<Ingredient, Double> theoreticalStock = new HashMap<>();

    private final Stack<ProductionRecord>  productionLog = new Stack<>();
    private final LinkedList<RefillRecord> refillLog     = new LinkedList<>();
    private final List<String>             alertMessages = new ArrayList<>();
    private final List<Ingredient>         lowStockList  = new ArrayList<>();

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================

    public InventoryManager() {
        ingredients = DatabaseManager.loadIngredients();
        coffeeTypes = DatabaseManager.loadCoffeeTypes(ingredients);

        for (Ingredient ing : ingredients) {
            currentStock.put(ing,     ing.getCurrentStock());
            theoreticalStock.put(ing, ing.getTheoreticalStock());
        }

        refreshAlerts();
    }

    // =========================================================================
    //  VALIDATE PRODUCTION
    // =========================================================================

    public String validateProduction(CoffeeType coffee, int qty) {
        for (Map.Entry<Ingredient, Double> entry : coffee.getRecipe().entrySet()) {
            Ingredient ing    = entry.getKey();
            double     needed = entry.getValue() * qty;
            double     avail  = currentStock.getOrDefault(ing, 0.0);
            if (needed > 0 && avail < needed) {
                return String.format(
                    "Insufficient %s. Need %.1f %s but only %.1f %s available.",
                    ing.getDisplayName(), needed, ing.getUnit(), avail, ing.getUnit());
            }
        }
        return null;
    }

    // =========================================================================
    //  PRODUCE
    // =========================================================================

    public void produce(CoffeeType coffee, int qty) {
        for (Map.Entry<Ingredient, Double> entry : coffee.getRecipe().entrySet()) {
            Ingredient ing    = entry.getKey();
            double     needed = entry.getValue() * qty;
            currentStock.put(ing,     currentStock.getOrDefault(ing, 0.0)     - needed);
            theoreticalStock.put(ing, theoreticalStock.getOrDefault(ing, 0.0) - needed);
        }

        productionLog.push(new ProductionRecord(coffee.getDisplayName(), qty));
        refreshAlerts();

        DatabaseManager.logProduction(coffee.getDisplayName(), qty);
        for (Ingredient ing : ingredients) {
            DatabaseManager.saveStock(ing, currentStock.get(ing), theoreticalStock.get(ing));
        }
    }

    // =========================================================================
    //  REFILL
    // =========================================================================

    public void refill(Ingredient ingredient, double amount) {
        currentStock.put(ingredient,     currentStock.getOrDefault(ingredient, 0.0)     + amount);
        theoreticalStock.put(ingredient, theoreticalStock.getOrDefault(ingredient, 0.0) + amount);

        refillLog.addLast(new RefillRecord(ingredient, amount));
        refreshAlerts();

        DatabaseManager.logRefill(ingredient, amount);
        DatabaseManager.saveStock(ingredient, currentStock.get(ingredient), theoreticalStock.get(ingredient));
    }

    // =========================================================================
    //  VARIANCE
    // =========================================================================

    public Map<Ingredient, Double> calculateVariance(Map<Ingredient, Double> physicalCounts) {
        Map<Ingredient, Double> variance = new HashMap<>();
        for (Ingredient ing : ingredients) {
            double theoretical = theoreticalStock.getOrDefault(ing, 0.0);
            double physical    = physicalCounts.getOrDefault(ing, theoretical);
            variance.put(ing, theoretical - physical);
        }
        return variance;
    }

    // =========================================================================
    //  MAX PRODUCIBLE
    // =========================================================================

    public int maxProducible(CoffeeType coffee) {
        int max = Integer.MAX_VALUE;
        for (Map.Entry<Ingredient, Double> entry : coffee.getRecipe().entrySet()) {
            double perCup = entry.getValue();
            if (perCup > 0) {
                double avail = currentStock.getOrDefault(entry.getKey(), 0.0);
                max = Math.min(max, (int)(avail / perCup));
            }
        }
        return (max == Integer.MAX_VALUE) ? 0 : max;
    }

    // =========================================================================
    //  RELOAD - call after adding/removing ingredients or coffee types
    // =========================================================================

    public void reload() {
        ingredients = DatabaseManager.loadIngredients();
        coffeeTypes = DatabaseManager.loadCoffeeTypes(ingredients);
        currentStock.clear();
        theoreticalStock.clear();
        for (Ingredient ing : ingredients) {
            currentStock.put(ing,     ing.getCurrentStock());
            theoreticalStock.put(ing, ing.getTheoreticalStock());
        }
        refreshAlerts();
    }

    // =========================================================================
    //  ALERTS
    // =========================================================================

    private void refreshAlerts() {
        alertMessages.clear();
        lowStockList.clear();
        for (Ingredient ing : ingredients) {
            double stock = currentStock.getOrDefault(ing, 0.0);
            if (stock < ing.getLowStockThreshold()) {
                lowStockList.add(ing);
                alertMessages.add(String.format(
                    "LOW STOCK: %s  (%.1f %s remaining, threshold: %.0f %s)",
                    ing.getDisplayName(),
                    stock, ing.getUnit(),
                    ing.getLowStockThreshold(), ing.getUnit()));
            }
        }
    }

    // =========================================================================
    //  GETTERS
    // =========================================================================

    public double                   getStock(Ingredient ing)            { return currentStock.getOrDefault(ing, 0.0);     }
    public double                   getTheoreticalStock(Ingredient ing) { return theoreticalStock.getOrDefault(ing, 0.0); }
    public List<Ingredient>         getIngredients()                    { return ingredients;                             }
    public List<CoffeeType>         getCoffeeTypes()                    { return coffeeTypes;                             }
    public Stack<ProductionRecord>  getProductionLog()                  { return productionLog;                           }
    public LinkedList<RefillRecord> getRefillLog()                      { return refillLog;                               }
    public List<String>             getAlerts()                         { return alertMessages;                           }
    public boolean                  hasAlerts()                         { return !alertMessages.isEmpty();                }
    public List<Ingredient>         getLowStockList()                   { return lowStockList;                            }
    public int                      getLowStockCount()                  { return lowStockList.size();                     }
}