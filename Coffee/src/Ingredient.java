public class Ingredient {

    private final int    id;
    private final String name;
    private final String unit;
    private final double lowStockThreshold;
    private final double currentStock;
    private final double theoreticalStock;

    public Ingredient(int id, String name, String unit,
                      double lowStockThreshold,
                      double currentStock,
                      double theoreticalStock) {
        this.id                = id;
        this.name              = name;
        this.unit              = unit;
        this.lowStockThreshold = lowStockThreshold;
        this.currentStock      = currentStock;
        this.theoreticalStock  = theoreticalStock;
    }

    public int    getId()                 { return id;                }
    public String getDisplayName()        { return name;              }
    public String getUnit()               { return unit;              }
    public double getLowStockThreshold()  { return lowStockThreshold; }
    public double getCurrentStock()       { return currentStock;      }
    public double getTheoreticalStock()   { return theoreticalStock;  }

    @Override
    public String toString() { return name; }
}