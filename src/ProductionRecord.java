import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductionRecord {

    private final String        coffeeName;
    private final int           quantity;
    private final LocalDateTime timestamp;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ProductionRecord(String coffeeName, int quantity) {
        this.coffeeName = coffeeName;
        this.quantity   = quantity;
        this.timestamp  = LocalDateTime.now();
    }

    public String getCoffeeName() { return coffeeName; }
    public int    getQuantity()   { return quantity;   }

    @Override
    public String toString() {
        return String.format("[%s]  %-14s  x%d",
            timestamp.format(FMT), coffeeName, quantity);
    }
}