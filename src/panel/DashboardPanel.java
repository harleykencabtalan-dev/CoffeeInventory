import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final InventoryManager inventoryManager;
    private final Color COLOR_BG     = new Color(245, 245, 240);
    private final Color COLOR_CARD   = Color.WHITE;
    private final Color COLOR_TEXT   = new Color(80, 70, 60);
    private final Color COLOR_KHAKI  = new Color(225, 215, 195);
    private final Color COLOR_BROWN  = new Color(120, 90, 70);
    

    public DashboardPanel(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 245, 240));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        build();
    }

    private void build() {
        // --- HEADER ---
        
        setBackground(COLOR_BG); 
    
   
        JPanel overviewContainer = new JPanel(new BorderLayout(0, 10));
        overviewContainer.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("SYSTEM OVERVIEW");
        titleLabel.setForeground(COLOR_BROWN);
         titleLabel.setFont(new Font("SansSerif", Font.BOLD, 40));

        JLabel subStatusLabel = new JLabel(">_ STATUS: NOMINAL | LAST UPDATE: 11:26:50 PM");
        subStatusLabel.setForeground(new Color(150, 140, 130));
        subStatusLabel.setFont(new Font("Monospaced", Font.PLAIN, 20));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subStatusLabel, BorderLayout.CENTER);

        // --- STAT CARDS ---
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setOpaque(false);

        int totalIngredients = inventoryManager.getIngredients().size();
        int criticalCount    = inventoryManager.getLowStockList().size();
        int outOfStockCount  = 0;
        for (Ingredient ing : inventoryManager.getIngredients()) {
            if (inventoryManager.getStock(ing) <= 0) outOfStockCount++;
        }
        int productionToday = inventoryManager.getProductionLog().size();

        statsRow.add(createStatCard("TOTAL INGREDIENTS",  String.valueOf(totalIngredients), "Active tracked items", COLOR_BROWN));
        statsRow.add(createStatCard("CRITICAL STOCK",     String.valueOf(criticalCount),    "Below threshold",      new Color(160, 100, 40)));
        statsRow.add(createStatCard("OUT OF STOCK",       String.valueOf(outOfStockCount),  "Depleted items",new Color(180, 80, 80)));
        statsRow.add(createStatCard("TODAY'S PRODUCTION", String.valueOf(productionToday),  "Completed runs",     new Color(80, 120, 80)));
        overviewContainer.add(headerPanel, BorderLayout.NORTH);
        overviewContainer.add(statsRow,    BorderLayout.CENTER);

        // --- LOWER CONTENT ---
        JPanel lowerContent = new JPanel(new BorderLayout(20, 0));
        lowerContent.setOpaque(false);

        // Alerts
        JPanel alertsPanel = createSectionPanel("⚠ ACTIVE ALERTS");
        alertsPanel.setPreferredSize(new Dimension(0, 180));

        JPanel alertsContent = new JPanel();
        alertsContent.setLayout(new BoxLayout(alertsContent, BoxLayout.Y_AXIS));
        alertsContent.setBackground(COLOR_CARD);
        alertsContent.setOpaque(true);


        for (String alert : inventoryManager.getAlerts()) {
            JLabel alertLabel = new JLabel("• " + alert.toUpperCase());
            alertLabel.setForeground(new Color(180, 80, 90));
            alertLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
            alertLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));
            alertsContent.add(alertLabel);
        }

        JScrollPane scroll = new JScrollPane(alertsContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        alertsPanel.add(scroll, BorderLayout.CENTER);

        // Capability Map
        JPanel capabilityMap = createSectionPanel("☕ PRODUCTION CAPABILITY MAP");

        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (CoffeeType coffee : inventoryManager.getCoffeeTypes()) {
            int max    = inventoryManager.maxProducible(coffee);
            String status = max > 0 ? "SUPPLY ABUNDANT" : "CANNOT PRODUCE";
            grid.add(createCoffeeCard(coffee.getDisplayName(), max + " MUGS", status));
        }
        capabilityMap.add(grid, BorderLayout.CENTER);

        // Left column
        JPanel leftColumn = new JPanel(new BorderLayout(0, 20));
        leftColumn.setOpaque(false);
        leftColumn.add(alertsPanel,   BorderLayout.NORTH);
        leftColumn.add(capabilityMap, BorderLayout.CENTER);

        // Activity log (right)
        JPanel logPanel = createSectionPanel(">_ SYSTEM ACTIVITY LOG");
        logPanel.setPreferredSize(new Dimension(350, 0));

        lowerContent.add(leftColumn, BorderLayout.CENTER);
        lowerContent.add(logPanel,   BorderLayout.EAST);

        add(overviewContainer, BorderLayout.NORTH);
        add(lowerContent,      BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String value, String subText, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(COLOR_CARD);
      card.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 200), 1));
        
JPanel inner = new JPanel(new BorderLayout(0, 5));
    inner.setOpaque(false);
    inner.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title);
    lblTitle.setForeground(new Color(140, 130, 120)); 
    lblTitle.setFont(new Font("SansSerif", Font.BOLD, 11));


      JLabel lblValue = new JLabel(value);
    lblValue.setForeground(accentColor); 
    lblValue.setFont(new Font("SansSerif", Font.BOLD, 36));

JLabel lblSubText = new JLabel(subText);
    lblSubText.setForeground(new Color(160, 160, 160));
    lblSubText.setFont(new Font("SansSerif", Font.PLAIN, 13));


       inner.add(lblTitle, BorderLayout.NORTH);
    inner.add(lblValue, BorderLayout.CENTER);
    inner.add(lblSubText, BorderLayout.SOUTH);
    
    card.add(inner);
    return card;
}



    static JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createLineBorder(new Color(210, 200, 180)));

        JLabel label = new JLabel(title);

       label.setForeground(new Color(200, 50, 50));

        label.setFont(new Font("SansSerif", Font.BOLD, 25));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createCoffeeCard(String name, String mugs, String status) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(15, 25, 35));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 40, 50)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setForeground(new Color(100, 150, 100));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        JLabel mugLabel = new JLabel(mugs);
        mugLabel.setForeground(Color.WHITE);
        mugLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mugLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        mugLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(nameLabel,   BorderLayout.NORTH);
        card.add(statusLabel, BorderLayout.CENTER);
        card.add(mugLabel,    BorderLayout.EAST);
        return card;
    }
}