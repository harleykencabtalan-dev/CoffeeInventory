

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RefillRecord {

    private final Ingredient ingredient;
    private final double     amount;
    private final LocalDateTime timestamp;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RefillRecord(Ingredient ingredient, double amount) {
        this.ingredient = ingredient;
        this.amount     = amount;
        this.timestamp  = LocalDateTime.now();
    }

    public Ingredient getIngredient() { return ingredient; }
    public double     getAmount()     { return amount;     }

    @Override
    public String toString() {
        return String.format("[%s]  +%-8.1f%-3s  %s",
            timestamp.format(FMT), amount,
            ingredient.getUnit(), ingredient.getDisplayName());
    }
}
