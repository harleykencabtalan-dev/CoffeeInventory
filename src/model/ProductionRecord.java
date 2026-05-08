package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductionRecord {

    private final String        coffeeName;
    private final int           quantity;
    private final String        size;
    private final String        customizations;
    private final LocalDateTime timestamp;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ─── Full constructor (used by ProductionPanel with size + customizations) ─
    public ProductionRecord(String coffeeName, int quantity, String size, String customizations) {
        this.coffeeName     = coffeeName;
        this.quantity       = quantity;
        this.size           = size;
        this.customizations = customizations;
        this.timestamp      = LocalDateTime.now();
    }

    // ─── Backwards-compatible constructor (size defaults to "Medium") ─────────
    public ProductionRecord(String coffeeName, int quantity) {
        this(coffeeName, quantity, "Medium", "—");
    }

    public String        getCoffeeName()     { return coffeeName;     }
    public int           getQuantity()       { return quantity;       }
    public String        getSize()           { return size;           }
    public String        getCustomizations() { return customizations; }
    public LocalDateTime getTimestamp()      { return timestamp;      }

    @Override
    public String toString() {
        return String.format("[%s]  %-14s  x%d  (%s)",
            timestamp.format(FMT), coffeeName, quantity, size);
    }
}