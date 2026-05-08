package panel.production;

import model.CoffeeType;
import model.Ingredient;
import model.InventoryManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductionController {

    private final InventoryManager im;

    private static final String ICE_NAME   = "Ice";
    private static final double ICE_SMALL  = 100.0;
    private static final double ICE_MEDIUM = 150.0;
    private static final double ICE_LARGE  = 200.0;

    public ProductionController(InventoryManager im) {
        this.im = im;
    }

    // ─── Data accessors ───────────────────────────────────────────────────────
    public List<CoffeeType> getCoffeeTypes() {
        return im.getCoffeeTypes();
    }

    public List<Ingredient> getIngredients() {
        return im.getIngredients();
    }

    // ─── ID-based stock lookup ────────────────────────────────────────────────
    // Always look up by ID so stale object references never cause 0.0 returns
    public double getStock(Ingredient ing) {
        for (Ingredient live : im.getIngredients()) {
            if (live.getId() == ing.getId()) {
                return im.getStock(live);
            }
        }
        return 0.0;
    }

    // ─── Find live ingredient by ID ───────────────────────────────────────────
    private Ingredient findById(int id) {
        for (Ingredient ing : im.getIngredients()) {
            if (ing.getId() == id) return ing;
        }
        return null;
    }

    // ─── Max producible ───────────────────────────────────────────────────────
    public int maxProducible(CoffeeType ct, double sizeMultiplier) {
        return im.maxProducible(ct, sizeMultiplier);
    }

    // ─── Size multiplier ──────────────────────────────────────────────────────
    public double getSizeMultiplier(String size) {
        return switch (size) {
            case "Small" -> 0.75;
            case "Large" -> 1.25;
            default      -> 1.0;
        };
    }

    // ─── Ice helpers ──────────────────────────────────────────────────────────
    public double getIceAmount(String size) {
        return switch (size) {
            case "Small" -> ICE_SMALL;
            case "Large" -> ICE_LARGE;
            default      -> ICE_MEDIUM;
        };
    }

    public Ingredient findIce() {
        for (Ingredient ing : im.getIngredients()) {
            if (ing.getDisplayName().equalsIgnoreCase(ICE_NAME)) {
                return ing;
            }
        }
        return null;
    }

    // ─── Stock validation ─────────────────────────────────────────────────────
    public String validateStock(List<OrderItem> orders) {
        StringBuilder err = new StringBuilder();
        Map<Integer, Double> totalNeeded = new LinkedHashMap<>(); // keyed by ingredient ID

        for (OrderItem o : orders) {
            double m = getSizeMultiplier(o.size);

            // Regular recipe ingredients — use ID as key to avoid stale ref issues
            for (Map.Entry<Ingredient, Double> e : o.recipe.entrySet()) {
                totalNeeded.merge(e.getKey().getId(), e.getValue() * m, Double::sum);
            }

            // Ice for iced orders
            if ("Iced".equalsIgnoreCase(o.temp)) {
                Ingredient ice = findIce();
                if (ice != null) {
                    totalNeeded.merge(ice.getId(), getIceAmount(o.size), Double::sum);
                } else {
                    err.append("• Ice ingredient not found in inventory.\n");
                }
            }
        }

        // Check availability using live ingredient references
        for (Map.Entry<Integer, Double> e : totalNeeded.entrySet()) {
            Ingredient live = findById(e.getKey());
            if (live == null) continue;
            double avail = im.getStock(live);
            if (avail < e.getValue()) {
                err.append(String.format("• %s: need %.2f %s, have %.2f\n",
                        live.getDisplayName(),
                        e.getValue(), live.getUnit(), avail));
            }
        }

        return err.toString();
    }

    // ─── Execute production ───────────────────────────────────────────────────
    public void produce(List<OrderItem> orders) {
        Ingredient ice = findIce();

        for (OrderItem o : orders) {
            // Rebuild recipe using LIVE ingredient references (matched by ID)
            // This ensures im.produce() can find them in the stock HashMap
            Map<Ingredient, Double> finalRecipe = new LinkedHashMap<>();

            for (Map.Entry<Ingredient, Double> e : o.recipe.entrySet()) {
                Ingredient live = findById(e.getKey().getId());
                if (live != null) finalRecipe.put(live, e.getValue());
            }

            // Inject ice for iced orders
            if ("Iced".equalsIgnoreCase(o.temp) && ice != null) {
                finalRecipe.merge(ice, getIceAmount(o.size), Double::sum);
            }

            String producedName = o.coffeeType.getDisplayName()
                + (o.isCustomized                       ? " (Custom)" : "")
                + ("Iced".equalsIgnoreCase(o.temp)      ? " [Iced]"   : "");

            CoffeeType synth = new CoffeeType(
                o.coffeeType.getId(),
                producedName,
                Collections.unmodifiableMap(finalRecipe)
            );

            im.produce(synth, getSizeMultiplier(o.size), 1);
        }
    }
}